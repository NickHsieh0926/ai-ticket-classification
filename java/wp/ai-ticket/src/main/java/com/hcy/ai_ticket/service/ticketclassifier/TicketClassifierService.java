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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.util.DebugTrace;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Service
public class TicketClassifierService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

//    private final String endpoint = "http://fastapi:8000/predict";
    private final String endpoint = "http://localhost:8000/predict";
    private HttpClient client;
    private ObjectMapper mapper;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @PostConstruct
    public void init() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
        
        LOGGER.info("TicketClassifierService initialized with HttpClient and ObjectMapper.");
    }


    public PredictionResult predictAndSave(String ticketText) throws Exception {
    	
        String traceId = MDC.get("traceId");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", ticketText);
        String json = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-Trace-Id", traceId != null ? traceId : "") // 傳遞給 Python
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        LOGGER.info("Sending request to AI model");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = mapper.readValue(response.body(), Map.class);

        PredictionResult prediction = new PredictionResult(
                (String) result.get("input"),
                (String) result.get("predicted_label"),
                ((Number) result.get("confidence")).doubleValue()
        );

        saveToDatabase(prediction, traceId);

        return prediction;
    }

    public List<PredictionResult> predictBatchAndSave(List<String> texts) throws Exception {
        List<PredictionResult> results = new ArrayList<>();
        for (String t : texts) {
            results.add(predictAndSave(t));
        }
        return results;
    }

    private void saveToDatabase(PredictionResult result, String traceId) {
        Ticket ticket = new Ticket();
        ticket.setContent(result.getInput());
        ticket.setCategory(result.getPredictedLabel());
        ticket.setConfidence(String.valueOf(result.getConfidence()));
        ticket.setTraceId(traceId);

        ticketRepository.save(ticket);
        LOGGER.info("Ticket saved to database, ID: {}", ticket.getId());
    }
    
}
