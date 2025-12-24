from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from src.predict import predict_text, predict_batch

app = FastAPI(title="Ticket Classification API")

# -------------------------
# Request Models
# -------------------------
class PredictRequest(BaseModel):
    text: str

class PredictBatchRequest(BaseModel):
    texts: List[str]

# -------------------------
# Endpoints
# -------------------------
@app.get("/")
def root():
    return {"message": "Ticket Classification API is running"}

@app.post("/predict")
def single_predict(request: PredictRequest):
    result = predict_text(request.text)
    return result

@app.post("/predict_batch")
def batch_predict(request: PredictBatchRequest):
    results = predict_batch(request.texts)
    return results
