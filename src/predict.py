# src/predict.py
import joblib
from pathlib import Path
from typing import List, Dict
from src.preprocessing import clean_text
from src.config import CATEGORY_MAPPING

BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "models" / "ticket_classifier.joblib"

# 載入模型
model = joblib.load(MODEL_PATH)

def predict_text(text: str) -> Dict:
    text_clean = clean_text(text)

    # confidence
    try:
        probs = model.predict_proba([text_clean])[0]
        pred_idx = probs.argmax()
        confidence = round(float(probs[pred_idx]), 2)
        pred_label = model.classes_[pred_idx]
        mapped_label = CATEGORY_MAPPING.get(pred_label, pred_label)
    except:
        confidence = None

    return {
        "input": text,
        "predicted_label": mapped_label,
        "confidence": confidence
    }

def predict_batch(texts: List[str]) -> List[Dict]:
    return [predict_text(t) for t in texts]

# CLI 測試
if __name__ == "__main__":
    samples = [
        "My internet is down and cannot connect.",
        "I need a refund for my last invoice."
    ]
    for s in samples:
        print(predict_text(s))
