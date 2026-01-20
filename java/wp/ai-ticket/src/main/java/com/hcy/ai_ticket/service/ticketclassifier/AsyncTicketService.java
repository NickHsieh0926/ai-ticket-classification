package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class AsyncTicketService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTicketService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

//  private final String endpoint = "http://fastapi:8000/predict";
	private final String endpoint = "http://localhost:8000/predict";

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private TicketRepository ticketRepository;

	@Async("ticketExecutor")
	public void runAiPredictionTask(String content, String traceId, int itemIndex) {
		
		if (MDC.get("traceId") == null && traceId != null) {
	        MDC.put("traceId", traceId);
	    }
		
		String spanId = traceId + "-" + itemIndex;
		MDC.put("spanId", spanId); 

		try {
			TRACE.message(">>> 開始處理批次子任務 [spanId:{}]", spanId);

			Map<String, String> requestBody = new HashMap<>();
			requestBody.put("text", content);

			@SuppressWarnings("unchecked")
			Map<String, Object> result = restTemplate.postForObject(endpoint, requestBody, Map.class);

			if (result == null) {
				throw new RuntimeException("AI model returned empty response");
			}
			
			saveToDatabase(result, traceId, spanId);

			TRACE.message("<<< [spanId:{}] 子任務處理成功，Category: {}", spanId, (String) result.get("predicted_label"));

		} catch (Exception e) {
			LOGGER.error("!!! 子任務處理失敗: {}", e.getMessage());
		} finally {
			MDC.remove("spanId");
		}
	}

	private void saveToDatabase(Map<String, Object> result, String traceId, String spanId) {
		Ticket ticket = new Ticket();
		ticket.setContent((String) result.get("input"));
		ticket.setCategory((String) result.get("predicted_label"));
		ticket.setConfidence((String) result.get("confidence"));
		ticket.setTraceId(traceId); 
		ticket.setSpanId(spanId); 
		ticket.setStatus("SUCCESS");
		
		ticketRepository.save(ticket);
		TRACE.message("Ticket saved to database, ID: {}", ticket.getId());
	}

}
