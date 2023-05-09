package com.socialnetwork.parrot.core.services.interfaces;

import com.socialnetwork.parrot.application.models.requests.post.CreatePostRequest;
import com.socialnetwork.parrot.application.models.requests.post.UpdatePostRequest;
import com.socialnetwork.parrot.core.entities.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostServiceInterface {
    ResponseEntity<String> createPost(CreatePostRequest request);
    ResponseEntity<String> createPostPhoto(List<MultipartFile> photos, String content);
    ResponseEntity<String> updatePost(UUID id, UpdatePostRequest request);
    ResponseEntity<String> deletePost(UUID id);
    ResponseEntity<?> getFeedPost();
    ResponseEntity<?> getFeedByEmailUserPost(String email);
    ResponseEntity<String> likePost(UUID id);
    ResponseEntity<String> dislikePost(UUID id);

    Optional<Post> getPostById(UUID id);
    void savePost(Post post);
}
