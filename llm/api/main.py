import logging
import nltk
from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
from typing import List
from utils.logger_config import trace_id_var, span_id_var
from src.predict import predict_text, predict_batch
from src.llm_predict import llm_predict_text
from rag.retriever import store_embedding


def download_nltk_resources():
    try:
        nltk.download("wordnet")
        nltk.download("omw-1.4")
        from nltk.stem import WordNetLemmatizer

        lemmatizer = WordNetLemmatizer()
        lemmatizer.lemmatize("test")
        print("NLTK resources loaded successfully.")
    except Exception as e:
        print(f"Error loading NLTK: {e}")


download_nltk_resources()
logger = logging.getLogger(__name__)
app = FastAPI(title="Ticket Classification API")


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
def llm_predict(request: LlmPredictRequest, background_tasks: BackgroundTasks):
    logger.info("正在執行 LLM + RAG predict 運算...")
    result = llm_predict_text(request.text)
    background_tasks.add_task(
        store_embedding, trace_id_var.get(), request.text, result["predicted_label"]
    )
    return result
