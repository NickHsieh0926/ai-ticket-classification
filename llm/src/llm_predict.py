import os
import json
import re
import google.generativeai as genai
import logging
import asyncio
import time
from aiolimiter import AsyncLimiter
from google.api_core.exceptions import ResourceExhausted
from rag.retriever import retrieve_similar
from rag.semantic_cache import semantic_cache_get, semantic_cache_set

logger = logging.getLogger(__name__)
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("gemini-2.5-flash-lite")

CATEGORIES = ["Billing", "Technical", "Account", "General"]
RATE_LIMITER = AsyncLimiter(max_rate=12, time_period=60)

# 熔斷器共享狀態
_CIRCUIT = {
    "open": False,
    "open_until": 0.0,
    "consecutive_429": 0,
}
CIRCUIT_OPEN_THRESHOLD = 5
CIRCUIT_COOLDOWN_SECS = 30
MAX_RETRIES = 3
RETRY_DELAY_SECS = 2.0


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
        result = await _call_llm_with_retry(prompt, text, similar)
        # 3. 寫入 Semantic Cache
        semantic_cache_set(text, result)
    except Exception as e:
        result = {
            "input": text,
            "predicted_label": "LLM error",
            "confidence": "0.0",
            "reasoning": f"LLM error: {str(e)}",
            "model": "gemini-2.5-flash-lite",
            "rag_used": False,
            "cache_type": "none",
            "status": "error",
        }

    return result


async def _call_llm_with_retry(prompt: str, text: str, similar: list) -> dict:
    if _CIRCUIT["open"]:
        remaining = _CIRCUIT["open_until"] - time.monotonic()
        if remaining > 0:
            logger.error("Circuit OPEN：Gemini 429 冷卻中，約 %.0f 秒後恢復", remaining)
            raise RuntimeError(
                f"Circuit OPEN：Gemini 429 冷卻中，約 {remaining:.0f} 秒後恢復"
            )
        _CIRCUIT["open"] = False
        _CIRCUIT["consecutive_429"] = 0
        logger.info("Circuit HALF-OPEN：嘗試恢復")

    for attempt in range(MAX_RETRIES):
        if _CIRCUIT["open"] and time.monotonic() < _CIRCUIT["open_until"]:
            raise RuntimeError("Circuit OPEN mid-retry：本筆快速失敗")

        try:
            logger.info("控制並發 LLM 請求...")
            async with RATE_LIMITER:
                response = await model.generate_content_async(
                    prompt
                )  # 配合worker.mq_consumer 改成非同步等待

            _CIRCUIT["consecutive_429"] = 0

            json_str = re.search(r"\{.*\}", response.text, re.DOTALL).group()
            parsed = json.loads(json_str)
            return {
                "input": text,
                "predicted_label": parsed.get("predicted_label", "General"),
                "confidence": str(parsed.get("confidence", 0.0)),
                "reasoning": parsed.get("reasoning", ""),
                "model": "gemini-2.5-flash-lite",
                "rag_used": len(similar) > 0,
                "cache_type": "none",
                "status": "success",
            }
        except ResourceExhausted:
            _CIRCUIT["consecutive_429"] += 1
            logger.warning(
                "Gemini 429，第 %d 次嘗試，連續 429 計數=%d",
                attempt + 1,
                _CIRCUIT["consecutive_429"],
            )

            # 達到閾值 → 開路 
            if _CIRCUIT["consecutive_429"] >= CIRCUIT_OPEN_THRESHOLD:
                _CIRCUIT["open"] = True
                _CIRCUIT["open_until"] = time.monotonic() + CIRCUIT_COOLDOWN_SECS
                logger.error(
                    "Circuit OPEN：連續 %d 次 429，冷卻 %d 秒",
                    CIRCUIT_OPEN_THRESHOLD,
                    CIRCUIT_COOLDOWN_SECS,
                )
                raise RuntimeError(
                    f"Circuit OPEN：連續 {CIRCUIT_OPEN_THRESHOLD} 次 429，"
                    f"暫停 {CIRCUIT_COOLDOWN_SECS} 秒後自動恢復"
                )
            
            # 避免最後一次無意義等待
            if attempt < MAX_RETRIES - 1:
                logger.warning("未達閾值 → 固定間隔重試")
                await asyncio.sleep(RETRY_DELAY_SECS)

    # 防禦性設計，正常情況下不會被執行
    raise RuntimeError(f"Gemini 429：超過最大重試次數 {MAX_RETRIES}")
