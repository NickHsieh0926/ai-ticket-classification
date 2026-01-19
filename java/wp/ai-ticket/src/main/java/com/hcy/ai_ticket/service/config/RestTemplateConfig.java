package com.hcy.ai_ticket.service.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();

		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectionRequestTimeout(10000);
		restTemplate.setRequestFactory(factory);

		restTemplate.getInterceptors().add((request, body, execution) -> {
			String traceId = MDC.get("traceId");
			if (traceId != null) {
				request.getHeaders().add("X-Trace-Id", traceId);
			}

			String spanId = MDC.get("spanId");
			if (spanId != null) {
				request.getHeaders().add("X-Span-Id", spanId);
			}

			return execution.execute(request, body);
		});

		return restTemplate;
	}
}