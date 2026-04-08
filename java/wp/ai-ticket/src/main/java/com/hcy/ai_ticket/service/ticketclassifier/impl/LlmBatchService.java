package com.hcy.ai_ticket.service.ticketclassifier.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.mq.dto.LlmTaskMessage;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.webSocket.dto.ProgressMessage;

@Service
public class LlmBatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LlmBatchService.class);
	private static final String BATCH_TOTAL_KEY = "batch:%s:total";
	private static final String BATCH_COMPLETED_KEY = "batch:%s:completed";

	private final RabbitTemplate rabbitTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	private final TicketRepository ticketRepository;
	private final SimpMessagingTemplate wsTemplate;
	private final ObjectMapper objectMapper;

	@Value("${mq.exchange.direct}")
	private String directExchange;

	@Value("${mq.queue.llm-task}")
	private String llmTaskQueue;

	public LlmBatchService(RabbitTemplate rabbitTemplate, RedisTemplate<String, String> redisTemplate,
			TicketRepository ticketRepository, SimpMessagingTemplate wsTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.redisTemplate = redisTemplate;
		this.ticketRepository = ticketRepository;
		this.wsTemplate = wsTemplate;
		this.objectMapper = objectMapper;
	}

	public void dispatchBatch(List<String> texts, String traceId) {
		int total = texts.size();
		redisTemplate.opsForValue().set(String.format(BATCH_TOTAL_KEY, traceId), String.valueOf(total),
				Duration.ofHours(24));

		LOGGER.info("[LlmBatch] 開始派發 traceId={}, total={}", traceId, total);

		for (int i = 0; i < texts.size(); i++) {
			String text = texts.get(i);
			String spanId = traceId + "-" + (i + 1);
			String cacheKey = buildCacheKey(text);

			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				LOGGER.info("[LlmBatch] Cache HIT, spanId={}", spanId);
				handleCacheHit(cached, text, traceId, spanId);
			} else {
				LlmTaskMessage msg = new LlmTaskMessage(traceId, spanId, text, cacheKey);
				rabbitTemplate.convertAndSend(directExchange, llmTaskQueue, msg);
				LOGGER.info("[LlmBatch] MQ 派送, spanId={}", spanId);
			}
		}
	}

	public void pushProgress(String traceId, String label) {
		String totalStr = redisTemplate.opsForValue().get(String.format(BATCH_TOTAL_KEY, traceId));
		if (totalStr == null) {
			LOGGER.warn("[LlmBatch] 找不到 total，traceId={}", traceId);
			return;
		}
		int total = Integer.parseInt(totalStr);
		long completed = redisTemplate.opsForValue().increment(String.format(BATCH_COMPLETED_KEY, traceId));
		double percent = (double) completed / total * 100;
		boolean done = completed >= total;

		ProgressMessage msg = new ProgressMessage(traceId, String.valueOf(completed), String.valueOf(total),
				String.format("%.2f", percent), label, done ? "COMPLETED" : "PROCESSING");
		wsTemplate.convertAndSend("/topic/progress/" + traceId, msg);
		LOGGER.info("[LlmBatch] Progress {}/{} ({}%)", completed, total, String.format("%.2f", percent));

		if (done) {
			redisTemplate.delete(String.format(BATCH_TOTAL_KEY, traceId));
			redisTemplate.delete(String.format(BATCH_COMPLETED_KEY, traceId));
			LOGGER.info("[LlmBatch] 批次完成，清除 Redis traceId={}", traceId);
		}
	}

	private void handleCacheHit(String cached, String text, String traceId, String spanId) {
		try {
			PredictionResult result = objectMapper.readValue(cached, PredictionResult.class);
			saveTicket(text, result.getPredictedLabel(), String.valueOf(result.getConfidence()), traceId, spanId);
			pushProgress(traceId, result.getPredictedLabel());
		} catch (Exception e) {
			LOGGER.error("[LlmBatch] Cache HIT 處理失敗 spanId={}: {}", spanId, e.getMessage());
		}
	}

	private void saveTicket(String text, String category, String confidence, String traceId, String spanId) {
		Ticket ticket = new Ticket();
		ticket.setContent(text);
		ticket.setCategory(category);
		ticket.setConfidence(confidence);
		ticket.setTraceId(traceId);
		ticket.setSpanId(spanId);
		ticket.setStatus("SUCCESS");
		ticketRepository.save(ticket);
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
