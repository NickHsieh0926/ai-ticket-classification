package com.hcy.ai_ticket.service.ticketclassifier.impl.llm;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.util.CacheKeyUtils;

@Service("llmClient")
public class LlmApiClient implements IAiInferenceClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(LlmApiClient.class);

	private final RestTemplate restTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final String llmEndpoint;

	public LlmApiClient(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate,
			ObjectMapper objectMapper, @Value("${endpoint.llm}") String llmEndpoint) {
		this.restTemplate = restTemplate;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.llmEndpoint = llmEndpoint;
	}

	@Override
	public PredictionResult predict(String text) throws Exception {
		String key = CacheKeyUtils.buildLlmCacheKey(text);

		// Redis HIT
		String cached = redisTemplate.opsForValue().get(key);
		if (cached != null) {
			LOGGER.info("Redis cache HIT key: {}", key);
			return objectMapper.readValue(cached, PredictionResult.class);
		}

		// Redis MISS → call FastAPI
		Map<String, String> body = new HashMap<>();
		body.put("text", text);

		@SuppressWarnings("unchecked")
		Map<String, Object> result = restTemplate.postForObject(llmEndpoint, body, Map.class);
		if (result == null)
			throw new RuntimeException("LLM returned empty response");
		
		PredictionResult predictionResult = new PredictionResult(
				(String) result.get("input"),
				(String) result.get("predicted_label"),
				Double.valueOf((String) result.get("confidence")),
				(String) result.get("reasoning"),
				(String) result.get("model"),
				Boolean.TRUE.equals(result.get("rag_used")));
		
		// 寫入 Redis（TTL 24h）
		if("success".equals(result.get("status"))) {
			redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(predictionResult),
					Duration.ofHours(24));
			LOGGER.info("LLM 推論完成，status = success，寫入 Redis cacheKey:{}", key);
		}

		LOGGER.info("LLM 推論完成，label={}", predictionResult.getPredictedLabel());
		return predictionResult;
	}

	@Override
	public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
		List<PredictionResult> results = new ArrayList<>();
		for (String t : texts)
			results.add(predict(t));
		return results;
	}

}
