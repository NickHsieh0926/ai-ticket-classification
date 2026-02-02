package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.webSocket.dto.ProgressMessage;
import com.hcy.ai_ticket.service.webSocket.dto.TaskProgress;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class AsyncTicketService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTicketService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Value("${endpoint}")
	private String endpoint;

	private final Map<String, TaskProgress> progressMap = new ConcurrentHashMap<>();

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Async("ticketExecutor")
	public void runAiPredictionTask(String content, String traceId, int itemIndex, int total) {
		
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
			
			TaskProgress progress = progressMap.computeIfAbsent(traceId, k -> new TaskProgress());
			
		    int currentCount = progress.getCompletedCount().incrementAndGet();
		    
		    double currentPercent = ((double) currentCount / total * 100);
		    int intPercent = (int) currentPercent;

			if (currentCount == total || intPercent > progress.getLastSentPercent().get()) {

					if (progress.getLastSentPercent().getAndSet(intPercent) < intPercent || currentPercent == total) {
	
							ProgressMessage msg = new ProgressMessage(
									traceId,
					                String.valueOf(currentCount),
					                String.valueOf(total),
					                String.format("%.2f", currentPercent), 
					                (String) result.get("predicted_label"), 
					                currentCount >= total ? "COMPLETED" : "PROCESSING"
							);
		
							messagingTemplate.convertAndSend("/topic/progress/" + traceId, msg);
							LOGGER.info("發送進度: {}%", intPercent);
							
							if (currentCount == total) {
								progressMap.remove(traceId);
							}
					}
			}

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
