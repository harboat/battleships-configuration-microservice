package com.github.harboat.configuration;

import com.github.harboat.clients.game.ShipDto;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.*;

import java.util.Collection;
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Builder
@SuppressFBWarnings(value = "EI_EXPOSE_REP")
public class Player {
    private Collection<ShipDto> ships;
}
