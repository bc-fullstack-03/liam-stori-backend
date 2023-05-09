package com.socialnetwork.parrot.api.controllers;

import com.socialnetwork.parrot.application.models.requests.post.CreatePostRequest;
import com.socialnetwork.parrot.application.models.requests.post.UpdatePostRequest;
import com.socialnetwork.parrot.core.services.interfaces.PostServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/post")
public class PostController {
    @Autowired
    private PostServiceInterface _postService;

    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody CreatePostRequest request) {
        ResponseEntity<String> response = _postService.createPost(request);

        return response;
    }

    @PostMapping("/photo")
    public ResponseEntity<String> createPostPhoto(@RequestParam("photos") List<MultipartFile> photos, @RequestParam("content") String content) {
        ResponseEntity<String> response = _postService.createPostPhoto(photos, content);

        return response;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable("id")UUID id, @RequestBody UpdatePostRequest request) {
        ResponseEntity<String> response = _postService.updatePost(id, request);

        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id")UUID id) {
        ResponseEntity<String> response = _postService.deletePost(id);

        return response;
    }

    @GetMapping
    public ResponseEntity<?> getFeedPost() {
        ResponseEntity<?> response = _postService.getFeedPost();

        return response;
    }

    @GetMapping("/{email}")
    ResponseEntity<?> getFeedByEmailUserPost(@PathVariable("email")String email) {
        ResponseEntity<?> response = _postService.getFeedByEmailUserPost(email);

        return response;
    }

    @PutMapping("/{id}/like")
    ResponseEntity<String> likePost(@PathVariable("id")UUID id) {
        ResponseEntity<String> response = _postService.likePost(id);

        return response;
    }

    @PutMapping("/{id}/dislike")
    ResponseEntity<String> dislikePost(@PathVariable("id")UUID id) {
        ResponseEntity<String> response = _postService.dislikePost(id);

        return response;
    }
}
