package com.hcy.ai_ticket.service.ticketclassifier.impl.ml;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.impl.ab.AbProgressTracker;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.webSocket.WsProgressService;
import com.hcy.ai_ticket.service.webSocket.dto.TaskProgress;
import com.hcy.ai_ticket.service.webSocket.staticenum.TopicType;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class AsyncTicketService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTicketService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	private final Map<String, TaskProgress> progressMap = new ConcurrentHashMap<>();

	private final IAiInferenceClient mlClient;
	private final TicketRepository ticketRepository;
	private final AbProgressTracker abProgressTracker;
	private final WsProgressService wsProgressService;

	public AsyncTicketService(@Qualifier("mlClient") IAiInferenceClient mlClient, TicketRepository ticketRepository,
			AbProgressTracker abProgressTracker, WsProgressService wsProgressService) {
		this.mlClient = mlClient;
		this.ticketRepository = ticketRepository;
		this.abProgressTracker = abProgressTracker;
		this.wsProgressService = wsProgressService;
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

			PredictionResult result = mlClient.predict(content);

			saveToDatabase(result, traceId, spanId);

			TRACE.message("<<< [spanId:{}] 子任務處理成功，Category: {}", spanId, result.getPredictedLabel());

			if (abProgressTracker.isAbTask(traceId)) {
				abProgressTracker.itemComplete(traceId, result.getPredictedLabel());
			} else {
				TaskProgress progress = progressMap.computeIfAbsent(traceId, k -> new TaskProgress());
				int completed = progress.getCompletedCount().incrementAndGet();
				double percent = ((double) completed / total * 100);
				int intPercent = (int) percent;
				boolean done = completed >= total;

				if (completed == total || intPercent > progress.getLastSentPercent().get()) {
					if (progress.getLastSentPercent().getAndSet(intPercent) < intPercent || done) {

						wsProgressService.push(traceId, TopicType.PROGRESS, completed, total,
								result.getPredictedLabel(), done ? "COMPLETED" : "PROCESSING");

						LOGGER.info("發送進度: {}%", intPercent);

						if (completed == total) {
							progressMap.remove(traceId);
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("!!! 子任務處理失敗: {}", e.getMessage());
			if (abProgressTracker.isAbTask(traceId)) {
				abProgressTracker.itemComplete(traceId, "ML error");
			} else {
				TaskProgress progress = progressMap.computeIfAbsent(traceId, k -> new TaskProgress());
				int completed = progress.getCompletedCount().incrementAndGet();
				boolean done = completed >= total;
				wsProgressService.push(traceId, TopicType.PROGRESS, completed, total, "ML error",
						done ? "COMPLETED" : "PROCESSING");
				if (done)
					progressMap.remove(traceId);
			}
		} finally {
			MDC.remove("spanId");
			MDC.remove("traceId");
		}
	}

	private void saveToDatabase(PredictionResult result, String traceId, String spanId) {
		Ticket ticket = new Ticket();
		ticket.setContent(result.getInput());
		ticket.setCategory(result.getPredictedLabel());
		ticket.setConfidence(String.valueOf(result.getConfidence()));
		ticket.setTraceId(traceId);
		ticket.setSpanId(spanId);
		ticket.setStatus("success");
		ticket.setModelType("ml");

		ticketRepository.save(ticket);
		TRACE.message("Ticket saved to database, ID: {}", ticket.getId());
	}

}
