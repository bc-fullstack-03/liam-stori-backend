package com.socialnetwork.parrot.core.services.interfaces;

import com.socialnetwork.parrot.application.models.requests.comment.CreateCommentRequest;
import com.socialnetwork.parrot.application.models.requests.comment.UpdateCommentRequest;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CommentPostServiceInterface {
    ResponseEntity<String> createCommentPost(CreateCommentRequest request);
    ResponseEntity<String> updateCommentPost(UUID id, UpdateCommentRequest request);
    ResponseEntity<String> deleteCommentPost(UUID id);
    ResponseEntity<String> likeCommentPost(UUID id);
    ResponseEntity<String> dislikeCommentPost(UUID id);
}
