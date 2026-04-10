import os
import json
import re
import google.generativeai as genai
import logging
from rag.retriever import retrieve_similar
from rag.semantic_cache import semantic_cache_get, semantic_cache_set

logger = logging.getLogger(__name__)
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("gemini-2.5-flash-lite")

CATEGORIES = ["Billing", "Technical", "Account", "General"]


async def llm_predict_text(text: str) -> dict:
    logger.info("執行 llm_predict_text ")

    # 1. Semantic Cache
    cached = semantic_cache_get(text)
    if cached:
        logger.info("Semantic Cache 命中，similarity=%.4f", cached.get("similarity", 0))
        cached["cache_type"] = "semantic"
        return cached

    # 2. Cache 未命中 → RAG + LLM
    similar = retrieve_similar(text, top_k=3)
    rag_context = ""
    if similar:
        examples = "\n".join(
            [
                f'- "{s["content"]}" → {s["category"]} (similarity: {s["similarity"]:.2f})'
                for s in similar
            ]
        )
        rag_context = f"\nHere are similar tickets for reference:\n{examples}\n"

    prompt = f"""
You are an IT support ticket classifier.
Classify the following ticket into exactly one of these categories:
{", ".join(CATEGORIES)}
{rag_context}
Ticket: {text}

Reply in this JSON format only:
{{"predicted_label": "<category>", "confidence": <0.0-1.0>, "reasoning": "<brief reason>"}}
"""
    try:
        logger.info("LLM 請求...")
        response =  await model.generate_content_async(prompt) #配合worker.mq_consumer 改成非同步等待
        json_str = re.search(r"\{.*\}", response.text, re.DOTALL).group()
        parsed = json.loads(json_str)
        result = {
            "input": text,
            "predicted_label": parsed.get("predicted_label", "General"),
            "confidence": str(parsed.get("confidence", 0.0)),
            "reasoning": parsed.get("reasoning", ""),
            "model": "gemini-2.5-flash-lite",
            "rag_used": len(similar) > 0,
            "cache_type": "none",
        }
    except Exception as e:
        result = {
            "input": text,
            "predicted_label": "General",
            "confidence": "0.0",
            "reasoning": f"LLM error: {str(e)}",
            "model": "gemini-2.5-flash-lite",
            "rag_used": False,
            "cache_type": "none",
        }
    
    # 3. 寫入 Semantic Cache
    semantic_cache_set(text, result)    

    return result
