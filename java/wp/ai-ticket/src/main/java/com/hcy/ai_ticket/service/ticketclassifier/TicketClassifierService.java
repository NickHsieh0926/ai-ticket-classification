package com.hcy.ai_ticket.service.ticketclassifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

import tools.jackson.databind.ObjectMapper;

public class TicketClassifierService {

    private final String endpoint = "http://localhost:8000/predict";
    private final HttpClient client;
    private final ObjectMapper mapper;
    
    public TicketClassifierService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
    }

    public PredictionResult predict(String ticketText) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", ticketText);
        String json = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        @SuppressWarnings("unchecked")
		Map<String, Object> result = mapper.readValue(response.body(), Map.class);

        return new PredictionResult(
                (String) result.get("input"),
                (String) result.get("predictedLabel"),
                ((Number) result.get("confidence")).doubleValue()
        );
    }
    
    
    public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
        List<PredictionResult> results = new ArrayList<>();
        for (String t : texts) {
            results.add(predict(t));
        }
        return results;
    }
    
    
}
