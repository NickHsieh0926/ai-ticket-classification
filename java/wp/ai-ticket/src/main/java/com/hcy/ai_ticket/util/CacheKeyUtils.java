package com.hcy.ai_ticket.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CacheKeyUtils {

	private static final String LLM_KEY_PREFIX = "llm:predict:";

	private CacheKeyUtils() {
	}

	public static String normalizeText(String text) {
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .trim();
	}

	public static String buildLlmCacheKey(String text) {
		try {
			String normalized = normalizeText(text);
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash)
				sb.append(String.format("%02x", b));
			return LLM_KEY_PREFIX + sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("MD5 計算失敗", e);
		}
	}

}
