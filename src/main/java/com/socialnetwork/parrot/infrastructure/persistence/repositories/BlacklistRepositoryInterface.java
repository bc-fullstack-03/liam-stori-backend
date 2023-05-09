package com.socialnetwork.parrot.infrastructure.persistence.repositories;

import com.socialnetwork.parrot.core.entities.Blacklist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface BlacklistRepositoryInterface extends MongoRepository<Blacklist, UUID> {
}
