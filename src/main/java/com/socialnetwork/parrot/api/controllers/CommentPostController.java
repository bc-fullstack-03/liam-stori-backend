package com.socialnetwork.parrot.api.controllers;

import com.socialnetwork.parrot.application.models.requests.comment.CreateCommentRequest;
import com.socialnetwork.parrot.application.models.requests.comment.UpdateCommentRequest;
import com.socialnetwork.parrot.core.services.interfaces.CommentPostServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/comment")
public class CommentPostController {
    @Autowired
    private CommentPostServiceInterface _commentPostService;

    @PostMapping
    public ResponseEntity<String> createComment(@RequestBody CreateCommentRequest request) {
        var response = _commentPostService.createCommentPost(request);

        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateComment(@PathVariable("id") UUID id, @RequestBody UpdateCommentRequest request) {
        ResponseEntity<String> response = _commentPostService.updateCommentPost(id, request);

        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable("id") UUID id) {
        ResponseEntity<String> response = _commentPostService.deleteCommentPost(id);

        return response;
    }

    @PutMapping("/{id}/like")
    ResponseEntity<String> likeComment(@PathVariable("id") UUID id) {
        ResponseEntity<String> response = _commentPostService.likeCommentPost(id);

        return response;
    }

    @PutMapping("/{id}/dislike")
    ResponseEntity<String> dislikeComment(@PathVariable("id") UUID id) {
        ResponseEntity<String> response = _commentPostService.dislikeCommentPost(id);

        return response;
    }
}
