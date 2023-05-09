package com.socialnetwork.parrot.application.services.post;

import com.socialnetwork.parrot.application.models.requests.comment.CreateCommentRequest;
import com.socialnetwork.parrot.application.models.requests.comment.UpdateCommentRequest;
import com.socialnetwork.parrot.application.validations.commentPost.CreateCommentPostRequestValidation;
import com.socialnetwork.parrot.core.entities.CommentPost;
import com.socialnetwork.parrot.core.entities.Post;
import com.socialnetwork.parrot.core.entities.User;
import com.socialnetwork.parrot.core.services.interfaces.CommentPostServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.UserServiceInterface;
import com.socialnetwork.parrot.infrastructure.persistence.repositories.PostRepositoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentPostService implements CommentPostServiceInterface {
    @Autowired
    private PostRepositoryInterface _postRepository;

    @Autowired
    private UserServiceInterface _userService;

    @Async
    @Override
    public ResponseEntity<String> createCommentPost(CreateCommentRequest request) {
        CreateCommentPostRequestValidation validation = new CreateCommentPostRequestValidation();
        try {
            Optional<Post> postOptional = getPostById(request.idPost);
            if(postOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            Post post = postOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            if(!validation.isValidCreateCommentRequest(request)) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }

            CommentPost commentPost = new CommentPost(request.idPost, user.getId(), request.content);

            post.getComments().add(commentPost);
            savePost(post);

            return ResponseEntity.status(HttpStatus.CREATED).body(commentPost.getId().toString());
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while creating the comment!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> updateCommentPost(UUID id, UpdateCommentRequest request) {
        try {
            List<Post> posts = getAllPosts();
            Optional<CommentPost> commentOptional  = getCommentById(posts, id);
            if(commentOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            CommentPost commentPost = commentOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(!userOptional.get().getId().equals(commentPost.getIdUser())) {
                return getUnauthorizedResponse("You cannot update this post!");
            }

            UUID idPost = commentPost.getIdPost();
            Optional<Post> postOptional = getPostById(idPost);
            Post post = postOptional.get();

            if(request.content == null) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }
            else {
                commentPost.setContent(request.content);
            }

            List<CommentPost> commentPosts = post.getComments();
            updateCommentContentById(commentPosts, id, request.getContent());

            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while updating the comment!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> deleteCommentPost(UUID id) {
        try {
            List<Post> posts = getAllPosts();
            Optional<CommentPost> commentOptional  = getCommentById(posts, id);
            if(commentOptional.isEmpty()) {
                return getCommentNotFoundResponse();
            }
            CommentPost commentPost = commentOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(!userOptional.get().getId().equals(commentPost.getIdUser())) {
                return getUnauthorizedResponse("You cannot delete this post!");
            }

            UUID idPost = commentPost.getIdPost();
            Optional<Post> postOptional = getPostById(idPost);
            Post post = postOptional.get();

            post.getComments().removeIf(c -> c.getId().equals(id));
            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while deleting the comment!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> likeCommentPost(UUID id) {
        try {
            List<Post> posts = getAllPosts();
            Optional<CommentPost> commentOptional  = getCommentById(posts, id);
            if(commentOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            CommentPost commentPost = commentOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            if(commentPost.getLikes().contains(user.getId())) {
                return getConflictResponse("Already liked this comment!");
            }

            UUID idPost = commentPost.getIdPost();
            Optional<Post> postOptional = getPostById(idPost);
            Post post = postOptional.get();

            List<CommentPost> commentPosts = post.getComments();
            updateCommentLikes(commentPosts, id, commentPost.getLikes());

            commentPost.getLikes().add(user.getId());
            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while like the comment!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> dislikeCommentPost(UUID id) {
        try {
            List<Post> posts = getAllPosts();
            Optional<CommentPost> commentOptional  = getCommentById(posts, id);
            if(commentOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            CommentPost commentPost = commentOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            if(!commentPost.getLikes().contains(user.getId())) {
                return getConflictResponse("Still don't like this comment!");
            }

            UUID idPost = commentPost.getIdPost();
            Optional<Post> postOptional = getPostById(idPost);
            Post post = postOptional.get();

            List<CommentPost> commentPosts = post.getComments();
            updateCommentLikes(commentPosts, id, commentPost.getLikes());

            commentPost.getLikes().remove(user.getId());
            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while dislike the comment!");
        }
    }


    /* ------- Métodos privados -------- */
    private Optional<Post> getPostById(UUID idPost) {
        return _postRepository.findById(idPost);
    }

    private void savePost(Post post){
        _postRepository.save(post);
    }

    private List<Post> getAllPosts() {
        return _postRepository.findAll();
    }

    private Optional<CommentPost> getCommentById(List<Post> posts, UUID id) {
        return posts.stream()
                .flatMap(post -> post.getComments().stream())
                .filter(commentPostPost -> commentPostPost.getId().equals(id))
                .findFirst();
    }

    private void updateCommentContentById(List<CommentPost> commentPosts, UUID id, String newContent) {
        for (CommentPost commentPost : commentPosts) {
            if (commentPost.getId().equals(id)) {
                commentPost.setContent(newContent);
                break;
            }
        }
    }

    private void updateCommentLikes(List<CommentPost> commentPosts, UUID id, List<UUID> likes) {
        for (CommentPost commentPost : commentPosts) {
            if (commentPost.getId().equals(id)) {
                commentPost.setLikes(likes);
                break;
            }
        }
    }

    private ResponseEntity<String> getPostNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post does not exist!");
    }

    private ResponseEntity<String> getUserNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
    }

    private ResponseEntity<String> getCommentNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment does not exist!");
    }

    private ResponseEntity<String> getNoContentResponse() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private ResponseEntity<String> getConflictResponse(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    private ResponseEntity<String> getBadRequestResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    private ResponseEntity<String> getUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }

    private ResponseEntity<String> getInternalServerErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
