package com.hcy.ai_ticket.service.mq;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.mq.dto.LlmResultMessage;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.impl.llm.LlmBatchService;

@Component
public class LlmResultConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LlmResultConsumer.class);

	private final RedisTemplate<String, String> redisTemplate;
	private final LlmBatchService llmBatchService;
	private final ObjectMapper objectMapper;

	public LlmResultConsumer(RedisTemplate<String, String> redisTemplate, LlmBatchService llmBatchService,
			ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.llmBatchService = llmBatchService;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = "${mq.queue.llm-result}")
	public void handleResult(LlmResultMessage msg) throws Exception {

//		throw new AmqpRejectAndDontRequeueException("模擬 Consumer 失敗");

		MDC.put("traceId", msg.getTraceId());
		MDC.put("spanId", msg.getSpanId());

		try {
			LOGGER.info("[LlmConsumer] 收到 LLM 結果，spanId={}, label={}", msg.getSpanId(), msg.getPredictedLabel());

			PredictionResult result = new PredictionResult(msg.getText(), msg.getPredictedLabel(),
					Double.valueOf(msg.getConfidence()), msg.getReasoning(), msg.getModel(), msg.isRagUsed());

			// 回寫 Redis （TTL 1 hr）
			if ("success".equals(msg.getStatus())) {
				redisTemplate.opsForValue().set(msg.getCacheKey(), objectMapper.writeValueAsString(result),
						Duration.ofHours(1));
				LOGGER.info("[LlmConsumer] 寫入 Redis cacheKey:{}", msg.getCacheKey());
			}

			// 寫入 DB
			llmBatchService.saveTicket(msg.getText(), msg.getPredictedLabel(), msg.getConfidence(), msg.getTraceId(),
					msg.getSpanId(), msg.getReasoning(), msg.getModel(), msg.isRagUsed(), msg.getStatus());

			// 推送進度（由 LlmBatchService 統一處理 Redis INCR + WebSocket）
			llmBatchService.pushProgress(msg.getTraceId(), msg.getPredictedLabel());

			LOGGER.info("[LlmConsumer] LLM 結果處理完成");

		} catch (Exception e) {
			LOGGER.error("[LlmConsumer] 處理 LLM 結果失敗", e);
			throw new AmqpException(e);
		} finally {
			MDC.remove("spanId");
			MDC.remove("traceId");
		}
	}
}
