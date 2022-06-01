package com.github.harboat.configuration;

import com.github.harboat.clients.game.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Builder
public class Configuration {
    @Id
    private String id;
    private String ownerId;
    private String roomId;
    private Size size;
    private Map<String, Player> playersConfiguration;
}
