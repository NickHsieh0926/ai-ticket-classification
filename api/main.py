import logging
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from utils.logger_config import trace_id_var
from src.predict import predict_text, predict_batch

logger = logging.getLogger(__name__)
app = FastAPI(title="Ticket Classification API")


# Middleware
@app.middleware("http")
async def log_middleware(request, call_next):
    tid = request.headers.get("X-Trace-Id", "n/a")
    token = trace_id_var.set(tid)
    try:
        return await call_next(request)
    finally:
        trace_id_var.reset(token)


# Request Models
class PredictRequest(BaseModel):
    text: str


class PredictBatchRequest(BaseModel):
    texts: List[str]


# Endpoints
@app.get("/")
def root():
    print(f"Logger Name: {logger.name}")
    print(f"Logger Level: {logger.getEffectiveLevel()}")
    logger.info("get Ticket Classification API status")
    return {"message": "Ticket Classification API is running"}


@app.post("/predict")
def single_predict(request: PredictRequest):
    logger.info("正在執行 AI predict 運算...")
    result = predict_text(request.text)
    return result


@app.post("/predict_batch")
def batch_predict(request: PredictBatchRequest):
    logger.info("正在執行 AI predict_batch 運算...")
    results = predict_batch(request.texts)
    return results
