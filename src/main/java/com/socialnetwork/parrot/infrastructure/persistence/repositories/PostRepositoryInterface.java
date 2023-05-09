package com.socialnetwork.parrot.infrastructure.persistence.repositories;

import com.socialnetwork.parrot.core.entities.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PostRepositoryInterface extends MongoRepository<Post, UUID> {
}
