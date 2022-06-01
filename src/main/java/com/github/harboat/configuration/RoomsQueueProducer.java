package com.github.harboat.configuration;

import com.github.harboat.clients.rooms.MarkFleetSet;
import com.github.harboat.clients.rooms.UnmarkFleetSet;
import com.github.harboat.rabbitmq.RabbitMQMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomsQueueProducer {

    private final RabbitMQMessageProducer producer;

    @Value("${rabbitmq.exchanges.rooms}")
    private String internalExchange;

    @Value("${rabbitmq.routing-keys.rooms}")
    private String roomsRoutingKey;

    public void setFleet(MarkFleetSet markFleetSet) {
        producer.publish(markFleetSet, internalExchange, roomsRoutingKey);
    }

    public void unsetFleet(UnmarkFleetSet unmarkFleetSet) {
        producer.publish(unmarkFleetSet, internalExchange, roomsRoutingKey);
    }
}

