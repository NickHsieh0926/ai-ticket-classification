package com.hcy.ai_ticket.service.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadConfig {

	@Bean(name = "ticketExecutor")
	public Executor ticketExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(100); 
		executor.setThreadNamePrefix("AI-Task-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		
		executor.setTaskDecorator(new MdcTaskDecorator());
		executor.initialize();
		return executor;
	}

	@Bean(name = "dispatcherExecutor")
	public Executor dispatcherExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(50); 
		executor.setThreadNamePrefix("CSV-Dispatcher-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		
		executor.setTaskDecorator(new MdcTaskDecorator());
		executor.initialize();
		return executor;
	}



}
