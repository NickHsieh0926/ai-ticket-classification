package com.hcy.ai_ticket.service.ticketclassifier.impl.llm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.impl.ab.AbProgressTracker;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.webSocket.WsProgressService;
import com.hcy.ai_ticket.service.webSocket.staticenum.TopicType;

@Service
public class LlmBatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LlmBatchService.class);
	private static final String BATCH_TOTAL_KEY = "batch:%s:total";
	private static final String BATCH_COMPLETED_KEY = "batch:%s:completed";
	private final ConcurrentHashMap<String, AtomicInteger> lastSentPercentMap = new ConcurrentHashMap<>();

	private final RedisTemplate<String, String> redisTemplate;
	private final TicketRepository ticketRepository;
	private final ObjectMapper objectMapper;
	private final AbProgressTracker abProgressTracker;
	private final WsProgressService wsProgressService;
	private final LlmDispatchService llmDispatchService;

	@Value("${mq.exchange.direct}")
	private String directExchange;

	@Value("${mq.queue.llm-task}")
	private String llmTaskQueue;

	public LlmBatchService(RedisTemplate<String, String> redisTemplate, TicketRepository ticketRepository,
			ObjectMapper objectMapper, AbProgressTracker abProgressTracker, WsProgressService wsProgressService,
			LlmDispatchService llmDispatchService) {
		this.redisTemplate = redisTemplate;
		this.ticketRepository = ticketRepository;
		this.objectMapper = objectMapper;
		this.abProgressTracker = abProgressTracker;
		this.wsProgressService = wsProgressService;
		this.llmDispatchService = llmDispatchService;
	}

	public void dispatchBatch(List<String> texts, String traceId) {
		int total = texts.size();
		redisTemplate.opsForValue().set(String.format(BATCH_TOTAL_KEY, traceId), String.valueOf(total),
				Duration.ofHours(24));
		lastSentPercentMap.put(traceId, new AtomicInteger(0));

		LOGGER.info("[LlmBatch] 開始派發 total={}", total);

		for (int i = 0; i < texts.size(); i++) {
			String text = texts.get(i);
			String spanId = traceId + "-" + (i + 1);
			String cacheKey = buildCacheKey(text);

			llmDispatchService.dispatchSingleItem(text, traceId, spanId, cacheKey);

		}
	}

	public void pushProgress(String traceId, String label) {
		if (abProgressTracker.isAbTask(traceId)) {
			abProgressTracker.itemComplete(traceId, label);
			return;
		}

		String totalStr = redisTemplate.opsForValue().get(String.format(BATCH_TOTAL_KEY, traceId));
		if (totalStr == null) {
			LOGGER.warn("[LlmBatch] 找不到 total");
			return;
		}
		int total = Integer.parseInt(totalStr);
		long completed = redisTemplate.opsForValue().increment(String.format(BATCH_COMPLETED_KEY, traceId));
		int intPercent = (int) (double) completed / total * 100;
		boolean done = completed >= total;

		AtomicInteger lastSent = lastSentPercentMap.get(traceId);
		if (lastSent != null && (done || intPercent > lastSent.get())) {
			if (lastSent.getAndSet(intPercent) < intPercent || done) {
				wsProgressService.push(traceId, TopicType.PROGRESS, completed, total, label,
						done ? "COMPLETED" : "PROCESSING");
				LOGGER.info("[LlmBatch] Progress {}/{} ({}%)", completed, total, intPercent);
			}
		}

		if (done) {
			redisTemplate.delete(String.format(BATCH_TOTAL_KEY, traceId));
			redisTemplate.delete(String.format(BATCH_COMPLETED_KEY, traceId));
			LOGGER.info("[LlmBatch] 批次完成，清除 Redis traceId={}", traceId);
		}
	}

	public void saveTicket(String text, String category, String confidence, String traceId, String spanId) {
		Ticket ticket = new Ticket();
		ticket.setContent(text);
		ticket.setCategory(category);
		ticket.setConfidence(confidence);
		ticket.setTraceId(traceId);
		ticket.setSpanId(spanId);
		ticket.setStatus("SUCCESS");
		ticket.setModelType("llm");
		ticketRepository.save(ticket);
	}

	public void handleCacheHit(String cached, String text, String traceId, String spanId) {
		try {
			PredictionResult result = objectMapper.readValue(cached, PredictionResult.class);
			LOGGER.info(
					"[LlmBatch] result info text:{}, result.getPredictedLabel:{}, String.valueOf(result.getConfidence()):{}, traceId:{}, spanId:{}",
					text, result.getPredictedLabel(), String.valueOf(result.getConfidence()), traceId, spanId);
			saveTicket(text, result.getPredictedLabel(), String.valueOf(result.getConfidence()), traceId, spanId);
			pushProgress(traceId, result.getPredictedLabel());
		} catch (Exception e) {
			LOGGER.error("[LlmBatch] Cache HIT 處理失敗: {}", e.getMessage());
		}
	}

	private String buildCacheKey(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash)
				sb.append(String.format("%02x", b));
			return "llm:predict:" + sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("MD5 計算失敗", e);
		}
	}
}
