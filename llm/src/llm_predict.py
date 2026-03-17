import os
import json
import re
import google.generativeai as genai

genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("gemini-2.5-flash-lite")

CATEGORIES = ["Billing", "Technical", "Account", "General"]


def llm_predict_text(text: str) -> dict:
    prompt = f"""
You are an IT support ticket classifier.
Classify the following ticket into exactly one of these categories:
{", ".join(CATEGORIES)}

Ticket: {text}

Reply in this JSON format only:
{{"predicted_label": "<category>", "confidence": <0.0-1.0>, "reasoning": "<brief reason>"}}
"""
    try:
        response = model.generate_content(prompt)
        json_str = re.search(r"\{.*\}", response.text, re.DOTALL).group()
        parsed = json.loads(json_str)
        result = {
            "input": text,
            "predicted_label": parsed.get("predicted_label", "General"),
            "confidence": str(parsed.get("confidence", 0.0)),
            "reasoning": parsed.get("reasoning", ""),
            "model": "gemini-2.5-flash-lite",
        }
    except Exception as e:
        result = {
            "input": text,
            "predicted_label": "General",
            "confidence": "0.0",
            "reasoning": f"LLM error: {str(e)}",
            "model": "gemini-2.5-flash-lite",
        }

    return result
