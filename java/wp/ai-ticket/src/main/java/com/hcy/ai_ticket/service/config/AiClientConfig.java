package com.hcy.ai_ticket.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.util.DebugTrace;

@Configuration
public class AiClientConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AiClientConfig.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Bean
    @Primary
    public IAiInferenceClient aiClient(
            @Value("${ai.mode:ml}") String mode,
            @Qualifier("mlClient") IAiInferenceClient mlClient,
            @Qualifier("llmClient") IAiInferenceClient llmClient) {
        LOGGER.info("AI Mode: {}", mode);
        return "llm".equals(mode) ? llmClient : mlClient;
    }
}
