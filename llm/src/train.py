import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import joblib
from pathlib import Path

from preprocessing import preprocess_corpus

# =========================
# 設定路徑
# =========================
DATA_PATH = Path("data/processed/tickets_clean.csv")
MODEL_DIR = Path("models")
MODEL_DIR.mkdir(exist_ok=True)

MODEL_PATH = MODEL_DIR / "ticket_classifier.joblib"

# =========================
# 主流程
# =========================
def main():
    print("Loading data...")
    df = pd.read_csv(DATA_PATH)

    X = df["ticket_text"]
    y = df["category"]

    print("Preprocessing text...")
    X_clean = preprocess_corpus(X.tolist())

    print("Splitting data...")
    X_train, X_test, y_train, y_test = train_test_split(
        X_clean, y, 
        test_size=0.2, 
        random_state=42, 
        stratify=y
    )

    print("Training model...")

    svc = LinearSVC()

    clf = CalibratedClassifierCV(svc, cv=5)

    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            max_features=5000,
            ngram_range=(1, 2)
        )),
        ("clf", clf)
    ])

    pipeline.fit(X_train, y_train)

    print("Evaluation:")
    y_pred = pipeline.predict(X_test)
    print("Accuracy:", accuracy_score(y_test, y_pred))
    print(classification_report(y_test, y_pred))

    print("Saving model...")
    joblib.dump(pipeline, MODEL_PATH)

    print(f"Training complete. Model saved to {MODEL_PATH}")

# =========================
# 入口點
# =========================
if __name__ == "__main__":
    main()
