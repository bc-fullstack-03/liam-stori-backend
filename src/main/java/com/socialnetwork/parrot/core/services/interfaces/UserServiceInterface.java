package com.socialnetwork.parrot.core.services.interfaces;

import com.socialnetwork.parrot.application.models.requests.user.*;
import com.socialnetwork.parrot.core.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

public interface UserServiceInterface {
    ResponseEntity<String> createUser(CreateUserRequest request);
    ResponseEntity<String> updateUser(UpdateUserRequest request);
    ResponseEntity<String> updatePhotoUser(MultipartFile photo);
    ResponseEntity<String> requestDisabledUser(UUID id);
    ResponseEntity<String> disabledUser(String email, HttpServletRequest request);
    ResponseEntity<String> sendFriendshipPetitionRequest(SendFriendshipPetitionRequest request);
    ResponseEntity<String> acceptedFriendshipPetitionRequest(FriendshipPetitionActionRequest request);
    ResponseEntity<String> declinedFriendshipPetitionRequest(FriendshipPetitionActionRequest request);
    ResponseEntity<String> followUser(FollowActionUserRequest request);
    ResponseEntity<String> unfollowUser(FollowActionUserRequest request);
    ResponseEntity<String> loginUser(LoginUserRequest request);
    ResponseEntity<String> logoutUser(HttpServletRequest request);
    ResponseEntity<String> resetPassword(ResetPasswordUserRequest request);
    ResponseEntity<?> getMyFriends();

    Optional<User> getUserRequested();
    Optional<User> getUserById(UUID idUser);
    Optional<User> getUserByEmail(String email);
    boolean isFollowing(UUID userId, UUID followingUserId);
}
