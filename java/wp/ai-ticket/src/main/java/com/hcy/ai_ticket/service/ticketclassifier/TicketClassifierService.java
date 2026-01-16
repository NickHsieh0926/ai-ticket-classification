package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class TicketClassifierService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

//    private final String endpoint = "http://fastapi:8000/predict";
    private final String endpoint = "http://localhost:8000/predict";
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TicketRepository ticketRepository;

    public PredictionResult predictAndSave(String ticketText) throws Exception {
    	
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", ticketText);

        LOGGER.info("Sending request to AI model");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.postForObject(endpoint, requestBody, Map.class);

            if (result == null) {
                throw new RuntimeException("AI model returned empty response");
            }

            PredictionResult prediction = new PredictionResult(
                    (String) result.get("input"),
                    (String) result.get("predicted_label"),
                    ((Number) result.get("confidence")).doubleValue()
            );

            String traceId = MDC.get("traceId");
            saveToDatabase(prediction, traceId);

            return prediction;

        } catch (Exception e) {
            LOGGER.error("AI model call failed: {}", e.getMessage());
            throw e; 
        }
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
