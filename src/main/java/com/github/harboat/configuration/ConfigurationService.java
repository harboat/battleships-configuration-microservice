package com.github.harboat.configuration;

import com.github.harboat.clients.configuration.*;
import com.github.harboat.clients.exceptions.BadRequest;
import com.github.harboat.clients.exceptions.ResourceNotFound;
import com.github.harboat.clients.game.GameCreate;
import com.github.harboat.clients.game.ShipDto;
import com.github.harboat.clients.game.Size;
import com.github.harboat.clients.notification.EventType;
import com.github.harboat.clients.notification.NotificationRequest;
import com.github.harboat.clients.rooms.MarkFleetSet;
import com.github.harboat.clients.rooms.UnmarkFleetSet;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ConfigurationService {

    private ConfigurationRepository repository;
    private RoomsQueueProducer roomsQueueProducer;
    private CoreQueueProducer coreQueueProducer;
    private GameQueueProducer gameQueueProducer;
    private NotificationProducer notificationProducer;

    void create(ConfigurationCreate configurationCreate) {
        Size size = new Size(10, 10);
        repository.save(
            Configuration.builder()
                    .roomId(configurationCreate.roomId())
                    .ownerId(configurationCreate.playerId())
                    .size(size)
                    .playersConfiguration(
                            Map.of(
                                    configurationCreate.playerId(),
                                    Player.builder()
                                            .ships(List.of())
                                            .build()
                            )
                    )
                    .build()
        );
        coreQueueProducer.sendSize(
                new SetGameSize(configurationCreate.roomId(), configurationCreate.playerId(), size)
        );
    }

    void setSize(SetGameSize setGameSize) {
        Configuration configuration = getOwnerConfigurationFromRequest(setGameSize.roomId(), setGameSize.playerId());
        Map<String, Player> playersConfiguration = configuration.getPlayersConfiguration();
        playersConfiguration.keySet()
                .forEach(p ->
                        playersConfiguration.put(
                                p,
                                Player.builder()
                                        .ships(List.of())
                                        .build()
                        )
                );
        configuration.setSize(setGameSize.size());
        repository.save(configuration);
        roomsQueueProducer.unsetFleet(
                new UnmarkFleetSet(setGameSize.roomId(), setGameSize.playerId())
        );
        coreQueueProducer.sendSize(
                setGameSize
        );
    }

    @Transactional
    void markShipPlacement(SetShipsPosition setShipsPosition) {
        Configuration configuration = getPlayerConfigurationFormRequest(setShipsPosition.roomId(), setShipsPosition.playerId());
        configuration.getPlayersConfiguration().put(
                setShipsPosition.playerId(),
                Player.builder()
                        .ships(setShipsPosition.ships())
                        .build()
        );
        repository.save(configuration);
        roomsQueueProducer.setFleet(
                new MarkFleetSet(
                        setShipsPosition.roomId(),
                        setShipsPosition.playerId()
                )
        );
        notificationProducer.sendNotification(
                new NotificationRequest<>(setShipsPosition.playerId(), EventType.FLEET_CREATED, setShipsPosition.ships())
        );
    }

    void createGame(CreateGame createGame) {
        Configuration configuration = getOwnerConfigurationFromRequest(createGame.roomId(), createGame.playerId());
        Map<String, Collection<ShipDto>> playerConfiguration = configuration.getPlayersConfiguration().entrySet()
                        .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getShips()));
        gameQueueProducer.sendCreateGame(
                new GameCreate(configuration.getSize(), playerConfiguration)
        );
    }

    private Configuration getPlayerConfigurationFormRequest(String roomId, String playerId) {
        Configuration configuration = repository.findByRoomId(roomId).orElseThrow( () -> new BadRequest("Room not found!"));
        if (!configuration.getPlayersConfiguration().containsKey(playerId)) throw new BadRequest("You are not in this room!");
        return configuration;
    }

    private Configuration getOwnerConfigurationFromRequest(String roomId, String playerId) {
        Optional<Configuration> configuration = repository.findByRoomIdAndOwnerId(roomId, playerId);
        if (configuration.isEmpty()) throw new BadRequest("You are not an owner for this room!");
        return configuration.get();
    }

    public void playerJoin(ConfigurationPlayerJoin playerJoin) {
        Configuration configuration = repository.findByRoomId(playerJoin.roomId()).orElseThrow(() -> new ResourceNotFound("Couldn't find a configuration for the room!"));
        configuration.getPlayersConfiguration().put(
                playerJoin.playerId(),
                Player.builder()
                    .ships(List.of())
                    .build()
        );
        repository.save(configuration);
    }
}
