import os
import json
import asyncio
import aio_pika
import logging
from utils.logger_config import trace_id_var, span_id_var
from src.llm_predict import llm_predict_text
from rag.retriever import store_embedding

logger = logging.getLogger(__name__)

# get MQ env
RABBITMQ_URL = (
    f"amqp://{os.getenv('RABBITMQ_USER', 'guest')}:"
    f"{os.getenv('RABBITMQ_PASSWORD')}@"
    f"{os.getenv('RABBITMQ_HOST', 'rabbitmq')}/"
)

LLM_TASK_QUEUE = os.getenv("MQ_QUEUE_LLM_TASK", "llm.task")
LLM_RESULT_QUEUE = os.getenv("MQ_QUEUE_LLM_RESULT", "llm.result")


async def process_message(message: aio_pika.IncomingMessage, result_exchange):
    async with message.process():
        # raise Exception("模擬推論失敗")
        body = json.loads(message.body.decode())
        trace_id = body.get("traceId")
        span_id = body.get("spanId")
        text = body.get("text")
        cache_key = body.get("cacheKey")

        token_t = trace_id_var.set(trace_id)
        token_s = span_id_var.set(span_id)

        try:
            # RAG + LLM 推論
            result = await llm_predict_text(text)
            # pgvector 寫入
            store_embedding(trace_id, text, result["predicted_label"])

            # 推論結果發布到 llm.result Queue
            result_msg = {
                "traceId": trace_id,
                "spanId": span_id,
                "cacheKey": cache_key,
                "text": text,
                "predictedLabel": result["predicted_label"],
                "confidence": result["confidence"],
                "reasoning": result["reasoning"],
                "model": result["model"],
                "ragUsed": result["rag_used"],
                "cacheType": result.get("cache_type", "none"),
                "status": result["status"],
            }
            logger.info(f"[Consumer] Done | {result['predicted_label']}")

        except Exception as e:
            logger.error(f"[Consumer] Error: {e}")
            result_msg = {
                "traceId": trace_id,
                "spanId": span_id,
                "cacheKey": cache_key,
                "text": text,
                "predictedLabel": "error",
                "confidence": "0.0",
                "reasoning": f"Processing failed: {str(e)}",
                "model": "gemini-2.5-flash-lite",
                "ragUsed": False,
                "cacheType": "none",
                "status": "error",
            }
        finally:
            trace_id_var.reset(token_t)
            span_id_var.reset(token_s)

        await result_exchange.publish(
            aio_pika.Message(
                body=json.dumps(result_msg).encode(),
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
            ),
            routing_key=LLM_RESULT_QUEUE,
        )


async def main():
    connection = await aio_pika.connect_robust(RABBITMQ_URL)
    channel = await connection.channel()
    await channel.set_qos(prefetch_count=5)

    result_exchange = await channel.declare_exchange(
        "ticket.direct", aio_pika.ExchangeType.DIRECT, durable=True
    )

    task_queue = await channel.declare_queue(LLM_TASK_QUEUE, passive=True)
    await task_queue.consume(lambda msg: process_message(msg, result_exchange))

    logger.info("[Consumer] Waiting for messages...")
    # 保持執行
    await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())
