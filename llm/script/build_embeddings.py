import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from dotenv import load_dotenv

load_dotenv(".env.local")

from sqlalchemy import text  # noqa: E402
from utils.db_config import get_engine  # noqa: E402
from rag.retriever import store_embedding  # noqa: E402

engine = get_engine()

with engine.connect() as conn:
    rows = conn.execute(
        text(
            "SELECT trace_id, content, category FROM tickets WHERE category IS NOT NULL LIMIT 500"
        )
    ).fetchall()

total = len(rows)
for i, row in enumerate(rows):
    store_embedding(row.trace_id, row.content, row.category)
    if i % 50 == 0:
        print(f"Processed {i}/{total}...")

print(f"Done. Total: {total} embeddings stored.")
