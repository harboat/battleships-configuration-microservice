package com.github.harboat.configuration;

import com.github.harboat.clients.configuration.ConfigurationCreate;
import com.github.harboat.clients.configuration.CreateGame;
import com.github.harboat.clients.configuration.SetGameSize;
import com.github.harboat.clients.configuration.SetShipsPosition;
import com.github.harboat.clients.game.GameCreate;
import com.github.harboat.clients.game.ShipDto;
import com.github.harboat.clients.game.Size;
import com.github.harboat.clients.rooms.MarkFleetSet;
import com.github.harboat.clients.rooms.UnmarkFleetSet;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;
import static org.mockito.BDDMockito.*;

@Listeners({MockitoTestNGListener.class})
public class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository repository;
    @Mock
    private RoomsQueueProducer roomsQueueProducer;
    @Mock
    private CoreQueueProducer coreQueueProducer;
    @Mock
    private GameQueueProducer gameQueueProducer;
    @Mock
    private NotificationProducer notificationProducer;
    private ConfigurationService configurationService;
    private String roomId;
    private String playerId;


    @BeforeMethod
    public void setUp() {
        configurationService = new ConfigurationService(repository, roomsQueueProducer, coreQueueProducer,
                gameQueueProducer, notificationProducer);
        roomId = "testRoom";
        playerId = "testPlayer";
    }

    @Test
    public void createShouldSendProperSetGameSize() {
        //given
        ConfigurationCreate configurationCreate = new ConfigurationCreate(roomId, playerId);
        ArgumentCaptor<SetGameSize> captor = ArgumentCaptor.forClass(SetGameSize.class);
        //when
        configurationService.create(configurationCreate);
        verify(coreQueueProducer).sendSize(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new SetGameSize(roomId, playerId, new Size(10, 10)));
    }

    @Test
    public void setSizeShouldUnsetFleetWithProperUnmarkFleetSet() {
        //given
        Map<String, Player> playersConfiguration = new HashMap<>();
        Configuration configuration = Configuration.builder()
                .roomId(roomId)
                .ownerId(playerId)
                .playersConfiguration(playersConfiguration)
                .build();
        given(repository.findByRoomIdAndOwnerId(roomId, playerId)).willReturn(Optional.of(configuration));
        ArgumentCaptor<UnmarkFleetSet> captor = ArgumentCaptor.forClass(UnmarkFleetSet.class);
        SetGameSize setGameSize = new SetGameSize(roomId, playerId, new Size(10, 10));
        //when
        configurationService.setSize(setGameSize);
        verify(roomsQueueProducer).unsetFleet(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new UnmarkFleetSet(roomId, playerId));
    }

    @Test
    public void setSizeShouldSendSizeWithProperSetGameSize() {
        //given
        Map<String, Player> playersConfiguration = new HashMap<>() {{
            put(playerId, new Player());
        }};
        Configuration configuration = Configuration.builder()
                .roomId(roomId)
                .ownerId(playerId)
                .playersConfiguration(playersConfiguration)
                .build();
        given(repository.findByRoomIdAndOwnerId(roomId, playerId)).willReturn(Optional.of(configuration));
        ArgumentCaptor<SetGameSize> captor = ArgumentCaptor.forClass(SetGameSize.class);
        SetGameSize setGameSize = new SetGameSize(roomId, playerId, new Size(10, 10));
        //when
        configurationService.setSize(setGameSize);
        verify(coreQueueProducer).sendSize(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, setGameSize);
    }

    @Test
    public void markShipPlacementShouldSetFleetWithProperMarkFleetSet() {
        //given
        Map<String, Player> playersConfiguration = new HashMap<>() {{
            put(playerId, new Player());
        }};
        Configuration configuration = Configuration.builder()
                .roomId(roomId)
                .ownerId(playerId)
                .playersConfiguration(playersConfiguration)
                .build();
        given(repository.findByRoomId(roomId)).willReturn(Optional.of(configuration));
        ArgumentCaptor<MarkFleetSet> captor = ArgumentCaptor.forClass(MarkFleetSet.class);
        SetShipsPosition setShipsPosition = new SetShipsPosition(roomId, playerId, new ArrayList<>());
        //when
        configurationService.markShipPlacement(setShipsPosition);
        verify(roomsQueueProducer).setFleet(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new MarkFleetSet(roomId, playerId));
    }

    @Test
    public void createGameShouldSendWithProperGameCreate() {
        //given
        Map<String, Collection<ShipDto>> playersConfiguration = new HashMap<>() {{
            put(playerId, new ArrayList<>());
        }};
        Configuration configuration = Configuration.builder()
                .roomId(roomId)
                .ownerId(playerId)
                .playersConfiguration(new HashMap<>())
                .size(new Size(10, 10))
                .build();
        given(repository.findByRoomIdAndOwnerId(roomId, playerId)).willReturn(Optional.of(configuration));
        ArgumentCaptor<GameCreate> captor = ArgumentCaptor.forClass(GameCreate.class);
        CreateGame createGame = new CreateGame(roomId, playerId);
        //when
        configurationService.createGame(createGame);
        verify(gameQueueProducer).sendCreateGame(captor.capture());
        var actual = captor.getValue();
        //then
        assertEquals(actual, new GameCreate(new Size(10, 10), new HashMap<>()));
    }


}