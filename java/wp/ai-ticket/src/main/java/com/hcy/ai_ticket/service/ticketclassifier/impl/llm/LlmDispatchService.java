package com.hcy.ai_ticket.service.ticketclassifier.impl.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.mq.dto.LlmTaskMessage;

@Service
public class LlmDispatchService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LlmDispatchService.class);

	private final RedisTemplate<String, String> redisTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final LlmBatchService llmBatchService;

	@Value("${mq.exchange.direct}")
	private String directExchange;
	@Value("${mq.queue.llm-task}")
	private String llmTaskQueue;

	public LlmDispatchService(RedisTemplate<String, String> redisTemplate, RabbitTemplate rabbitTemplate,
			@Lazy LlmBatchService llmBatchService) { 
		this.redisTemplate = redisTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.llmBatchService = llmBatchService;
	}

	@Async("llmDispatchExecutor")
	public void dispatchSingleItem(String text, String traceId, String spanId, String cacheKey) {
		MDC.put("traceId", traceId);
		MDC.put("spanId", spanId);
		try {
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				LOGGER.info("[LlmDispatch] Cache HIT");
				llmBatchService.handleCacheHit(cached, text, traceId, spanId);
			} else {
				LlmTaskMessage msg = new LlmTaskMessage(traceId, spanId, text, cacheKey);
				rabbitTemplate.convertAndSend(directExchange, llmTaskQueue, msg);
				LOGGER.info("[LlmDispatch] MQ 派送");
			}
		} catch (Exception e) {
			LOGGER.error("[LlmDispatch] MQ 派送失敗", e);
			throw new AmqpRejectAndDontRequeueException(e);
		}finally {
			MDC.remove("spanId");
			MDC.remove("traceId");
		}
	}

}
