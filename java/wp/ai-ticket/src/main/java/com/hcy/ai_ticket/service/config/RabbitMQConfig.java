package com.hcy.ai_ticket.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${mq.exchange.direct}")
    private String directExchange;

    @Value("${mq.queue.llm-task}")
    private String llmTaskQueue;

    @Value("${mq.queue.llm-task-dlq}")
    private String llmTaskDlq;

    @Value("${mq.queue.llm-result}")
    private String llmResultQueue;

    @Value("${mq.queue.llm-result-dlq}")
    private String llmResultDlq;

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchange);
    }

    @Bean
    public Queue llmTaskDlq() {
        return QueueBuilder.durable(llmTaskDlq).build();
    }

    @Bean
    public Queue llmTaskQueue() {
        return QueueBuilder.durable(llmTaskQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", llmTaskDlq)
            .build();
    }

    @Bean
    public Binding llmTaskBinding() {
        return BindingBuilder.bind(llmTaskQueue()).to(directExchange()).with(llmTaskQueue);
    }

    @Bean
    public Queue llmResultDlq() {
        return QueueBuilder.durable(llmResultDlq).build();
    }

    @Bean
    public Queue llmResultQueue() {
        return QueueBuilder.durable(llmResultQueue)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", llmResultDlq)
            .build();
    }

    @Bean
    public Binding llmResultBinding() {
        return BindingBuilder.bind(llmResultQueue()).to(directExchange()).with(llmResultQueue);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
