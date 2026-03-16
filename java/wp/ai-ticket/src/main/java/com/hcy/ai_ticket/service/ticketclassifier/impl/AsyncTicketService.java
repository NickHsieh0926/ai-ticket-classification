package com.hcy.ai_ticket.service.ticketclassifier.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.webSocket.dto.ProgressMessage;
import com.hcy.ai_ticket.service.webSocket.dto.TaskProgress;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class AsyncTicketService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTicketService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	private final Map<String, TaskProgress> progressMap = new ConcurrentHashMap<>();

	private final IAiInferenceClient aiClient;
	private final TicketRepository ticketRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public AsyncTicketService(IAiInferenceClient aiClient, TicketRepository ticketRepository,
			SimpMessagingTemplate messagingTemplate) {
		this.aiClient = aiClient;
		this.ticketRepository = ticketRepository;
		this.messagingTemplate = messagingTemplate;
	}
	
	 @Async("ticketExecutor")
     public void runAiPredictionTask(String content, String traceId, int itemIndex, int total) {

         if (MDC.get("traceId") == null && traceId != null) {
             MDC.put("traceId", traceId);
         }
         String spanId = traceId + "-" + itemIndex;
         MDC.put("spanId", spanId);

         try {
             TRACE.message(">>> 開始處理批次子任務 [spanId:{}]", spanId);

             PredictionResult result = aiClient.predict(content);

             saveToDatabase(result, traceId, spanId);

             TRACE.message("<<< [spanId:{}] 子任務處理成功，Category: {}", spanId, result.getPredictedLabel());

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
                             result.getPredictedLabel(),
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

     private void saveToDatabase(PredictionResult result, String traceId, String spanId) {
         Ticket ticket = new Ticket();
         ticket.setContent(result.getInput());
         ticket.setCategory(result.getPredictedLabel());
         ticket.setConfidence(String.valueOf(result.getConfidence()));
         ticket.setTraceId(traceId);
         ticket.setSpanId(spanId);
         ticket.setStatus("SUCCESS");

         ticketRepository.save(ticket);
         TRACE.message("Ticket saved to database, ID: {}", ticket.getId());
     }

}
