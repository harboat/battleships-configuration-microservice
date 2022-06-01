package com.github.harboat.configuration;

import com.github.harboat.clients.configuration.SetGameSize;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoreQueueProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.core}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.core}")
    private String coreRoutingKey;

    public void sendSize(SetGameSize setGameSize) {
        producer.publish(setGameSize, internalExchange, coreRoutingKey);
    }
}
