package com.hcy.ai_ticket.service.ticketclassifier.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

@Service("mlClient") 
public class MlRestApiClient implements IAiInferenceClient {
	
    private final RestTemplate restTemplate;
    private final String endpoint;

    public MlRestApiClient(RestTemplate restTemplate,
                           @Value("${endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
    }

    @Override
    public PredictionResult predict(String text) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("text", text);
        Map<String, Object> result = restTemplate.postForObject(endpoint, body, Map.class);
        if (result == null) throw new RuntimeException("AI model returned empty response");
        return new PredictionResult(
            (String) result.get("input"),
            (String) result.get("predicted_label"),
            Double.valueOf((String) result.get("confidence"))
        );
    }

    @Override
    public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
        List<PredictionResult> results = new ArrayList<>();
        for (String t : texts) results.add(predict(t));
        return results;
    }
}
