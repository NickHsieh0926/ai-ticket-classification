package com.hcy.ai_ticket.service.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadConfig {

	@Bean("ticketExecutor")
	public Executor ticketExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); 
		executor.setMaxPoolSize(10); 
		executor.setQueueCapacity(100); 
		executor.setThreadNamePrefix("AI-Task-");

		executor.setTaskDecorator(new MdcTaskDecorator());
		executor.initialize();
		return executor;
	}

}
