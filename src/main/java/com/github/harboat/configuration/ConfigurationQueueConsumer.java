package com.github.harboat.configuration;

import com.github.harboat.clients.configuration.*;
import com.github.harboat.clients.exceptions.BadRequest;
import com.github.harboat.clients.exceptions.ResourceNotFound;
import com.github.harboat.clients.notification.EventType;
import com.github.harboat.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@RabbitListener(
        queues = "${rabbitmq.queues.config}"
)
public class ConfigurationQueueConsumer {
    private final ConfigurationService service;
    private NotificationProducer notificationProducer;

    @RabbitHandler
    public void consume(ConfigurationCreate configurationCreate) {
        service.create(configurationCreate);
    }

    @RabbitHandler
    public void consume(SetGameSize setGameSize) {
        try {
            service.setSize(setGameSize);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(setGameSize.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(SetShipsPosition setShipsPosition) {
        try {
            service.markShipPlacement(setShipsPosition);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(setShipsPosition.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(CreateGame createGame) {
        try {
            service.createGame(createGame);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(createGame.playerId(), EventType.EXCEPTION, e)
            );
        }
    }

    @RabbitHandler
    public void consume(ConfigurationPlayerJoin playerJoin) {
        try {
            service.playerJoin(playerJoin);
        } catch (BadRequest | ResourceNotFound e) {
            notificationProducer.sendNotification(
                    new NotificationRequest<>(playerJoin.playerId(), EventType.EXCEPTION, e)
            );
        }
    }
}
