package com.hcy.ai_ticket.service.mq;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcy.ai_ticket.service.mq.dto.LlmResultMessage;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.impl.LlmBatchService;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;

@Component
public class LlmResultConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LlmResultConsumer.class);

	private final RedisTemplate<String, String> redisTemplate;
	private final TicketRepository ticketRepository;
	private final LlmBatchService llmBatchService;
	private final ObjectMapper objectMapper;

	public LlmResultConsumer(RedisTemplate<String, String> redisTemplate, TicketRepository ticketRepository,
			LlmBatchService llmBatchService, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.ticketRepository = ticketRepository;
		this.llmBatchService = llmBatchService;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = "${mq.queue.llm-result}")
	public void handleResult(LlmResultMessage msg) throws Exception {

//		throw new AmqpRejectAndDontRequeueException("模擬 Consumer 失敗");

		MDC.put("traceId", msg.getTraceId());
		MDC.put("spanId", msg.getSpanId());

		try {
			LOGGER.info("收到 LLM 結果，spanId={}, label={}", msg.getSpanId(), msg.getPredictedLabel());

			// 回寫 Redis （TTL 1 hr）
			PredictionResult result = new PredictionResult(msg.getText(), msg.getPredictedLabel(),
					Double.valueOf(msg.getConfidence()));
			redisTemplate.opsForValue().set(msg.getCacheKey(), objectMapper.writeValueAsString(result),
					Duration.ofHours(1));

			// 寫入 DB
			Ticket ticket = new Ticket();
			ticket.setContent(msg.getText());
			ticket.setCategory(msg.getPredictedLabel());
			ticket.setConfidence(msg.getConfidence());
			ticket.setStatus("DONE");
			ticket.setTraceId(msg.getTraceId());
			ticket.setSpanId(msg.getSpanId());
			ticketRepository.save(ticket);

			// 推送進度（由 LlmBatchService 統一處理 Redis INCR + WebSocket）
			llmBatchService.pushProgress(msg.getTraceId(), msg.getPredictedLabel());

			LOGGER.info("LLM 結果處理完成，traceId={}", msg.getTraceId());

		} catch (Exception e) {
			LOGGER.error("處理 LLM 結果失敗", e);
			throw new AmqpRejectAndDontRequeueException(e);
		} finally {
			MDC.remove("spanId");
		}
	}
}
