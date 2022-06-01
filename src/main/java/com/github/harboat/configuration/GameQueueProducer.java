package com.github.harboat.configuration;

import com.github.harboat.clients.game.GameCreate;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameQueueProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.game}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.game}")
    private String gameRoutingKey;


    public void sendCreateGame(GameCreate gameCreate) {
        producer.publish(gameCreate, internalExchange, gameRoutingKey);
    }
}
