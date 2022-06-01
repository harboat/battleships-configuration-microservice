package com.github.harboat.configuration;

import com.github.harboat.clients.game.ShipDto;
import lombok.*;

import java.util.Collection;
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Builder
public class Player {
    private Collection<ShipDto> ships;
}
