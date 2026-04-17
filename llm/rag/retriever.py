import logging
from sqlalchemy import text
from utils.db_config import get_engine
from rag.embedder import embed

logger = logging.getLogger(__name__)
engine = get_engine()


def retrieve_similar(query: str, top_k: int = 3) -> list[dict]:
    logger.info("執行 RAG 檢索")
    query_embedding = embed(query)
    embedding_str = str(query_embedding)

    sql = text("""
        SELECT content, category,
                1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM ticket_embeddings
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :top_k
    """)

    # 一般寫法
    # results = []
    # for r in rows:
    #     results.append({
    #         "content": r.content,
    #         "category": r.category,
    #         "similarity": float(r.similarity),
    #     })
    # return results

    # List Comprehension 寫法
    with engine.connect() as conn:
        rows = conn.execute(sql, {"embedding": embedding_str, "top_k": top_k})
        return [
            {
                "content": r.content,
                "category": r.category,
                "similarity": float(r.similarity),
            }
            for r in rows
        ]


def store_embedding(trace_id: str | None, content: str, category: str | None) -> None:
    logger.info(f"Category 為 '{category}'")
    if not category or category == "LLM error":
        logger.info("Category 為 None 或 LLM error，不儲存到ticket_embeddings")
        return

    logger.info("儲存 result 至 ticket_embeddings")
    embedding = embed(content)
    embedding_str = str(embedding)

    sql = text("""
        INSERT INTO ticket_embeddings (trace_id, content, embedding, category)
        VALUES (:trace_id, :content, CAST(:embedding AS vector), :category)
    """)

    with engine.connect() as conn:
        conn.execute(
            sql,
            {
                "trace_id": trace_id,
                "content": content,
                "embedding": embedding_str,
                "category": category,
            },
        )
        conn.commit()
