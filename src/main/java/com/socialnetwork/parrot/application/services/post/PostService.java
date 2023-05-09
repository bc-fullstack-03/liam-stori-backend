package com.socialnetwork.parrot.application.services.post;

import com.socialnetwork.parrot.application.models.requests.post.CreatePostRequest;
import com.socialnetwork.parrot.application.models.requests.post.UpdatePostRequest;
import com.socialnetwork.parrot.application.models.responses.post.GetFeedCommentPostResponse;
import com.socialnetwork.parrot.application.models.responses.post.GetFeedPostResponse;
import com.socialnetwork.parrot.application.validations.post.CreatePostRequestValidation;
import com.socialnetwork.parrot.application.validations.post.UpdatePostRequestValidation;
import com.socialnetwork.parrot.core.entities.CommentPost;
import com.socialnetwork.parrot.core.entities.Friendship;
import com.socialnetwork.parrot.core.entities.Post;
import com.socialnetwork.parrot.core.entities.User;
import com.socialnetwork.parrot.core.enums.FriendshipStatus;
import com.socialnetwork.parrot.core.enums.PostVisibility;
import com.socialnetwork.parrot.core.enums.UserStatus;
import com.socialnetwork.parrot.core.services.interfaces.FileUploadServiceInterface;
import com.socialnetwork.parrot.infrastructure.persistence.repositories.PostRepositoryInterface;
import com.socialnetwork.parrot.core.services.interfaces.PostServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService implements PostServiceInterface {
    @Autowired
    private PostRepositoryInterface _postRepository;

    @Autowired
    private UserServiceInterface _userService;

    @Autowired
    private FileUploadServiceInterface _fileUploadService;

    @Async
    @Override
    public ResponseEntity<String> createPost(CreatePostRequest request) {
        CreatePostRequestValidation validation = new CreatePostRequestValidation();

        try {
            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse("User does not exist!");
            }
            User user = userOptional.get();

            if(!validation.isValidCreatePostRequest(request)) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }

            Post post = new Post(user.getId(), request.title, request.content, request.postVisibility);

            savePost(post);

            return ResponseEntity.status(HttpStatus.CREATED).body(post.getId().toString());
        }
        catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while creating the post!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> createPostPhoto(List<MultipartFile> photos, String content) {
        try {
            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse("User does not exist!");
            }
            User user = userOptional.get();
            Post post = new Post();
            List<String> photosList = new ArrayList<>();

            for(MultipartFile photo : photos) {
                String photoUri = "";

                var fileName = UUID.randomUUID() + "." + photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf(".") + 1);
                photoUri = _fileUploadService.upload(photo, fileName);

                photosList.add(photoUri);
            }

            post.setIdUser(user.getId());
            post.setContent(content);
            post.setPhotosUri(photosList);
            savePost(post);

            return ResponseEntity.status(HttpStatus.CREATED).body(post.getId().toString());
        } catch (Exception e) {
            return getInternalServerErrorResponse("");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> updatePost(UUID id, UpdatePostRequest request) {
        UpdatePostRequestValidation validation = new UpdatePostRequestValidation();

        try {
            Optional<Post> postOptional = getPostById(id);
            if(postOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            Post post = postOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(!userOptional.get().getId().equals(post.getIdUser())) {
                return getUnauthorizedResponse("You cannot update this post!");
            }

            if(!validation.isValidUpdatePostRequest(request)) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }

            post.setTitle(request.title);
            post.setContent(request.content);
            post.setPostVisibility(request.postVisibility);

            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while updating the post!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> deletePost(UUID id) {
        try {
            Optional<Post> postOptional = getPostById(id);
            if(postOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            Post post = postOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(!userOptional.get().getId().equals(post.getIdUser())) {
                return getUnauthorizedResponse("You cannot delete this post!");
            }

            _postRepository.delete(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while deleting the post!");
        }
    }

    @Override
    public ResponseEntity<?> getFeedPost() {
        try {
            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse("User does not exist!");
            }
            User user = userOptional.get();

            List<Friendship> friendships = user.getFriendship();
            List<Post> posts = _postRepository.findAll();

            List<GetFeedPostResponse> getFeedPostResponse = getFilteredFeedPosts(posts, friendships, user.getId());

            return ResponseEntity.status(HttpStatus.OK).body(getFeedPostResponse);
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while verifying posts!");
        }
    }

    @Override
    public ResponseEntity<?> getFeedByEmailUserPost(String email) {
        try {
            Optional<User> userOptional = _userService.getUserRequested();
            Optional<User> emailUserOptional = _userService.getUserByEmail(email);
            if(userOptional.isEmpty() || emailUserOptional.isEmpty()) {
                return getUserNotFoundResponse("Some user does not exist!");
            }
            User user = userOptional.get();
            User emailByPath = emailUserOptional.get();

            List<Friendship> friendships = user.getFriendship();
            List<UUID> following = userOptional.get().getFollowingIds();

            List<Post> posts = getPostsByUserId(emailByPath.getId());

            List<GetFeedPostResponse> getFeedPostResponse = getFilteredFeedPostByUserId(posts, following, friendships, emailByPath.getId());

            return ResponseEntity.status(HttpStatus.OK).body(getFeedPostResponse);
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while checking user posts!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> likePost(UUID id) {
        try {
            Optional<Post> postOptional = getPostById(id);
            if(postOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            Post post = postOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse("User does not exist!");
            }
            User user = userOptional.get();

            if(post.getLikes().contains(user.getId())){
                return getConflictResponse("You already liked this post!");
            }

            post.getLikes().add(user.getId());
            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while like the post!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> dislikePost(UUID id) {
        try {
            Optional<Post> postOptional = getPostById(id);
            if(postOptional.isEmpty()) {
                return getPostNotFoundResponse();
            }
            Post post = postOptional.get();

            Optional<User> userOptional = _userService.getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse("User does not exist!");
            }
            User user = userOptional.get();

            if(!post.getLikes().contains(user.getId())){
                return getConflictResponse("You already disliked this post!");
            }

            post.getLikes().remove(user.getId());
            savePost(post);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while dislike the post!");
        }
    }

    @Override
    public Optional<Post> getPostById(UUID id) {
        return _postRepository.findById(id);
    }

    @Override
    public void savePost(Post post) {
        _postRepository.save(post);
    }


    /* ------- Métodos privados -------- */
    private List<Post> getPostsByUserId(UUID id){
        return _postRepository.findAll().stream()
                .filter(post -> post.getIdUser().equals(id))
                .collect(Collectors.toList());
    }

    private List<GetFeedPostResponse> getFilteredFeedPosts(List<Post> posts, List<Friendship> friendships, UUID idUser) {
        return posts.stream()
                .filter(post -> {
                    if (post.getIdUser().equals(idUser)) {
                        return false;
                    }

                    PostVisibility visibility = post.getPostVisibility();
                    UUID postUserId = post.getIdUser();

                    boolean isFriend = false;
                    for (Friendship friend : friendships) {
                        if (friend.getStatus() == FriendshipStatus.ACCEPTED && friend.getIdFriend().equals(postUserId)) {
                            isFriend = true;
                            break;
                        }
                    }

                    boolean isFollowing = _userService.isFollowing(idUser, postUserId);
                    if (!isFriend && !isFollowing) {
                        return visibility == PostVisibility.PUBLIC;
                    } else if (isFriend) {
                        return visibility == PostVisibility.PUBLIC || visibility == PostVisibility.ONLY_FRIENDS ||
                                visibility == PostVisibility.FRIENDS_AND_FOLLOWERS;
                    } else if (isFollowing) {
                        return visibility == PostVisibility.PUBLIC || visibility == PostVisibility.ONLY_FOLLOWERS ||
                                visibility == PostVisibility.FRIENDS_AND_FOLLOWERS;
                    } else {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(post -> {
                    GetFeedPostResponse getFeedPostResponseMap = new GetFeedPostResponse();
                    Optional<User> userOptional = _userService.getUserById(post.getIdUser());
                    User user = userOptional.get();

                    getFeedPostResponseMap.setPhotoUri(user.getPhotoUri());
                    getFeedPostResponseMap.setAuthor(user.getFullName());
                    getFeedPostResponseMap.setTitle(post.getTitle());
                    getFeedPostResponseMap.setContent(post.getContent());
                    getFeedPostResponseMap.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    getFeedPostResponseMap.setTotalLikes(post.getLikes().size());

                    List<GetFeedCommentPostResponse> commentResponses;
                    if(post.getComments() != null) {
                        int totalComments = 0;
                        for(CommentPost comment: post.getComments()) {
                            Optional<User> userCommentOptional = _userService.getUserById(comment.getIdUser());
                            if(!userCommentOptional.get().getUserStatus().equals(UserStatus.DISABLED)) {
                                totalComments++;
                            }
                        }
                        getFeedPostResponseMap.setTotalComments(totalComments);

                        commentResponses = post.getComments().stream()
                                .map(comment -> {
                                    Optional<User> userCommentOptional = _userService.getUserById(comment.getIdUser());
                                    User userComment = userCommentOptional.get();

                                    if(userComment.getUserStatus().equals(UserStatus.DISABLED)) {
                                        return null;
                                    }

                                    GetFeedCommentPostResponse commentResponse = new GetFeedCommentPostResponse();

                                    commentResponse.setPhotoUri(userComment.getPhotoUri());
                                    commentResponse.setAuthor(userComment.getFullName());
                                    commentResponse.setContent(comment.getContent());
                                    commentResponse.setCreatedAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                                    commentResponse.setLikes(comment.getLikes().size());

                                    return commentResponse;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    }
                    else {
                        commentResponses = Collections.emptyList();
                    }

                    getFeedPostResponseMap.setComments(commentResponses);
                    return getFeedPostResponseMap;
                })
                .collect(Collectors.toList());
    }

    private List<GetFeedPostResponse> getFilteredFeedPostByUserId(List<Post> posts, List<UUID> following, List<Friendship> friendships, UUID idUser){
        return posts.stream()
                .filter(post -> {
                    Optional<User> userOptional = _userService.getUserById(idUser);
                    User user = userOptional.get();
                    if (user.getUserStatus().equals(UserStatus.DISABLED)) {
                        return false;
                    }

                    PostVisibility visibility = post.getPostVisibility();
                    UUID postUserId = post.getIdUser();

                    boolean isFriend = false;
                    for (Friendship friend : friendships) {
                        if (friend.getStatus() == FriendshipStatus.ACCEPTED && friend.getIdFriend().equals(postUserId)) {
                            isFriend = true;
                            break;
                        }
                    }

                    boolean isFollowing = _userService.isFollowing(idUser, postUserId);
                    if (!isFriend && !isFollowing) {
                        return visibility == PostVisibility.PUBLIC;
                    } else if (isFriend) {
                        return visibility == PostVisibility.PUBLIC || visibility == PostVisibility.ONLY_FRIENDS ||
                                visibility == PostVisibility.FRIENDS_AND_FOLLOWERS;
                    } else if (isFollowing) {
                        return visibility == PostVisibility.PUBLIC || visibility == PostVisibility.ONLY_FOLLOWERS ||
                                visibility == PostVisibility.FRIENDS_AND_FOLLOWERS;
                    } else {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(post -> {
                    GetFeedPostResponse getFeedPostResponseMap = new GetFeedPostResponse();
                    Optional<User> userPostOptional = _userService.getUserById(idUser);
                    User user = userPostOptional.get();

                    getFeedPostResponseMap.setPhotoUri(user.getPhotoUri());
                    getFeedPostResponseMap.setAuthor(user.getFullName());
                    getFeedPostResponseMap.setTitle(post.getTitle());
                    getFeedPostResponseMap.setContent(post.getContent());
                    getFeedPostResponseMap.setCreatedAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    getFeedPostResponseMap.setTotalLikes(post.getLikes().size());

                    List<GetFeedCommentPostResponse> commentResponses;
                    if(post.getComments() != null) {
                        int totalComments = 0;
                        for(CommentPost comment: post.getComments()) {
                            Optional<User> userCommentOptional = _userService.getUserById(comment.getIdUser());
                            if(!userCommentOptional.get().getUserStatus().equals(UserStatus.DISABLED)) {
                                totalComments++;
                            }
                        }
                        getFeedPostResponseMap.setTotalComments(totalComments);

                        commentResponses = post.getComments().stream()
                                .map(comment -> {
                                    if(user.getUserStatus().equals(UserStatus.DISABLED)) {
                                        return null;
                                    }

                                    GetFeedCommentPostResponse commentResponse = new GetFeedCommentPostResponse();

                                    commentResponse.setPhotoUri(user.getPhotoUri());
                                    commentResponse.setAuthor(user.getFullName());
                                    commentResponse.setCreatedAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                                    commentResponse.setLikes(comment.getLikes().size());

                                    return commentResponse;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    }
                    else {
                        commentResponses = Collections.emptyList();
                    }

                    getFeedPostResponseMap.setComments(commentResponses);
                    return getFeedPostResponseMap;
                })
                .collect(Collectors.toList());
    }

    private ResponseEntity<String> getPostNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post does not exist!");
    }

    private ResponseEntity<String> getUserNotFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    private ResponseEntity<String> getUnauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
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

    private ResponseEntity<String> getInternalServerErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
