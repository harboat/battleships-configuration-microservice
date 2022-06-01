package com.github.harboat.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationAppConfiguration {

    @Value("${rabbitmq.exchanges.config}")
    private String internalGameExchange;

    @Value("${rabbitmq.queues.config}")
    private String configQueue;

    @Value("${rabbitmq.routing-keys.config}")
    private String internalConfigRoutingKey;

    @Bean
    public TopicExchange internalTopicExchange() {
        return new TopicExchange(internalGameExchange);
    }

    @Bean
    public Queue configQueue() {
        return new Queue(configQueue);
    }

    @Bean
    public Binding internalToPlacementBinding() {
        return BindingBuilder
                .bind(configQueue())
                .to(internalTopicExchange())
                .with(internalConfigRoutingKey);
    }

}
