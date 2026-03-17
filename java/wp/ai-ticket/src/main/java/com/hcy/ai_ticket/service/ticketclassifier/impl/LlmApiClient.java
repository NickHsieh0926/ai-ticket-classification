package com.hcy.ai_ticket.service.ticketclassifier.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

@Service("llmClient")
public class LlmApiClient implements IAiInferenceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmApiClient.class);
    private static final long CACHE_TTL = 86400L; // 24h

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String llmEndpoint;

    public LlmApiClient(RestTemplate restTemplate,
                        RedisTemplate<String, String> redisTemplate,
                        ObjectMapper objectMapper,
                        @Value("${endpoint.llm}") String llmEndpoint) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.llmEndpoint = llmEndpoint;
    }

    @Override
    public PredictionResult predict(String text) throws Exception {
        String key = cacheKey(text);

        // 查 Redis 快取
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            LOGGER.info("Redis cache HIT: {}", key);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(cached, Map.class);
            return toPredictionResult(result);
        }

        // 呼叫 FastAPI
        LOGGER.info("Redis cache MISS, calling FastAPI LLM...");
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = restTemplate.postForObject(llmEndpoint, body, Map.class);
        if (result == null) throw new RuntimeException("LLM returned empty response");

        // 寫入 Redis（TTL 24h）
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), CACHE_TTL, TimeUnit.SECONDS);
        LOGGER.info("Redis cache SET: {}", key);

        return toPredictionResult(result);
    }

    @Override
    public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
        List<PredictionResult> results = new ArrayList<>();
        for (String t : texts) results.add(predict(t));
        return results;
    }

    private String cacheKey(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
        	sb.append(String.format("%02x", b));
        }
        return "llm:predict:" + sb.toString();
    }

    private PredictionResult toPredictionResult(Map<String, Object> result) {
        return new PredictionResult(
            (String) result.get("input"),
            (String) result.get("predicted_label"),
            Double.valueOf((String) result.get("confidence"))
        );
    }
}

