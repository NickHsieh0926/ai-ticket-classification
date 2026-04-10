import json
import logging
from sqlalchemy import text
from rag.embedder import embed
from utils.db_config import get_engine

logger = logging.getLogger(__name__)
engine = get_engine()

# 閾值
SIMILARITY_THRESHOLD = 0.95


# 查詢語意快取，找到相似度 > 閾值的結果
def semantic_cache_get(query_text: str) -> dict | None:
    logger.info("查詢語意快取 semantic_cache")
    embedding_str = str(embed(query_text))

    sql = text("""
        SELECT id, result_json,
               1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM semantic_cache
        WHERE 1 - (embedding <=> CAST(:embedding AS vector)) > :threshold
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT 1
    """)

    with engine.connect() as conn:
        row = conn.execute(
            sql, {"embedding": embedding_str, "threshold": SIMILARITY_THRESHOLD}
        ).fetchone()

        if row:
            conn.execute(
                text(
                    "UPDATE semantic_cache SET hit_count = hit_count + 1, last_hit_at = NOW() WHERE id = :id"
                ),
                {"id": row.id},
            )
            conn.commit()
            result = dict(row.result_json)
            result["semantic_cache_hit"] = True
            result["similarity"] = float(row.similarity)
            return result
    return None


# 將新結果存入語意快取
def semantic_cache_set(query_text: str, result: dict) -> None:
    logger.info("將新結果存入語意快取")
    embedding_str = str(embed(query_text))

    sql = text("""
        INSERT INTO semantic_cache (query_text, embedding, result_json)
        VALUES (:query_text, CAST(:embedding AS vector), :result_json)
    """)

    with engine.connect() as conn:
        conn.execute(
            sql,
            {
                "query_text": query_text,
                "embedding": embedding_str,
                "result_json": json.dumps(result),
            },
        )
        conn.commit()
