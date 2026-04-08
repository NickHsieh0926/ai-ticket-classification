import logging
import nltk
import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from typing import List
from utils.logger_config import trace_id_var, span_id_var
from src.predict import predict_text, predict_batch
from src.llm_predict import llm_predict_text
from rag.retriever import store_embedding

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    task = asyncio.create_task(start_consumer())
    await asyncio.sleep(1) #讓 start_consumer 有時間可以啟動
    yield
    task.cancel()


async def start_consumer():
    try:
        from worker.mq_consumer import main as consumer_main

        await consumer_main()
    except Exception as e:
        logger.error(f"[Consumer] 啟動失敗: {e}", exc_info=True)


def download_nltk_resources():
    try:
        nltk.download("wordnet")
        nltk.download("omw-1.4")
        from nltk.stem import WordNetLemmatizer

        lemmatizer = WordNetLemmatizer()
        lemmatizer.lemmatize("test")
        logger.info("NLTK resources loaded successfully.")
    except Exception as e:
        logger.error(f"Error loading NLTK: {e}")


download_nltk_resources()

app = FastAPI(title="Ticket Classification API", lifespan=lifespan)


# Middleware
@app.middleware("http")
async def log_middleware(request, call_next):
    trace_Id = request.headers.get("X-Trace-Id", "n/a")
    span_Id = request.headers.get("X-Span-Id", "n/a")
    token_tId = trace_id_var.set(trace_Id)
    token_sId = span_id_var.set(span_Id)
    try:
        return await call_next(request)
    finally:
        trace_id_var.reset(token_tId)
        span_id_var.reset(token_sId)


# Request Models
class PredictRequest(BaseModel):
    text: str


class PredictBatchRequest(BaseModel):
    texts: List[str]


class LlmPredictRequest(BaseModel):
    text: str


# Endpoints
@app.get("/")
def root():
    logger.info("get Ticket Classification API status")
    return {"message": "Ticket Classification API is running"}


@app.post("/predict")
def single_predict(request: PredictRequest):
    logger.info("正在執行 ML predict 運算...")
    result = predict_text(request.text)
    return result


@app.post("/predict_batch")
def batch_predict(request: PredictBatchRequest):
    logger.info("正在執行 ML predict_batch 運算...")
    results = predict_batch(request.texts)
    return results


@app.post("/llm_predict")
async def llm_predict(request: LlmPredictRequest, background_tasks: BackgroundTasks):
    logger.info("正在執行 LLM + RAG predict 運算...")
    result = await llm_predict_text(request.text)
    background_tasks.add_task(
        store_embedding, trace_id_var.get(), request.text, result["predicted_label"]
    )
    return result
