package com.socialnetwork.parrot.infrastructure.persistence.repositories;

import com.socialnetwork.parrot.core.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryInterface extends MongoRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
