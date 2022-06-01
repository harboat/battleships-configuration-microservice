package com.github.harboat.configuration;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends MongoRepository<Configuration, String> {
    Optional<Configuration> findByRoomIdAndOwnerId(String roomId, String ownerId);

    Optional<Configuration> findByRoomId(String roomId);
}
