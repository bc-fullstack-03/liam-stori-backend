package com.socialnetwork.parrot.api.controllers;

import com.socialnetwork.parrot.application.models.requests.user.*;
import com.socialnetwork.parrot.core.services.interfaces.UserServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/user")
public class UserController {
    @Autowired
    private UserServiceInterface _userService;

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request){
        ResponseEntity<String> response = _userService.createUser(request);

        return response;
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody UpdateUserRequest request) {
        ResponseEntity<String> response = _userService.updateUser(request);

        return response;
    }

    @PutMapping("/photo")
    public ResponseEntity<String> updatePhotoUser(@RequestParam("photo") MultipartFile photo) {
        ResponseEntity<String> response = _userService.updatePhotoUser(photo);

        return response;
    }

    @PutMapping("/{id}/request-delete")
    public ResponseEntity<String> requestDisabledUser(@PathVariable("id")UUID id) {
        ResponseEntity<String> response = _userService.requestDisabledUser(id);

        return response;
    }

    @PutMapping("/delete")
    public ResponseEntity<String> disabledUser(@RequestBody String email, HttpServletRequest request) {
        ResponseEntity<String> response = _userService.disabledUser(email, request);

        return response;
    }

    @PutMapping("/send-petition-friendship")
    public ResponseEntity<String> sendPetitionFriendship(@RequestBody SendFriendshipPetitionRequest request) {
        ResponseEntity<String> response = _userService.sendFriendshipPetitionRequest(request);

        return response;
    }

    @PutMapping("/accepted-petition-friendship")
    public ResponseEntity<String> acceptedFriendPetitionRequest(@RequestBody FriendshipPetitionActionRequest request) {
        ResponseEntity<String> response = _userService.acceptedFriendshipPetitionRequest(request);

        return response;
    }

    @PutMapping("/declined-petition-friendship")
    public ResponseEntity<String> declinedFriendPetitionRequest(@RequestBody FriendshipPetitionActionRequest request) {
        ResponseEntity<String> response = _userService.declinedFriendshipPetitionRequest(request);

        return response;
    }

    @PutMapping("/follow-user")
    public ResponseEntity<String> followUser(@RequestBody FollowActionUserRequest request) {
        ResponseEntity<String> response = _userService.followUser(request);

        return response;
    }

    @PutMapping("/unfollow-user")
    public ResponseEntity<String> unfollowUser(@RequestBody FollowActionUserRequest request) {
        ResponseEntity<String> response = _userService.unfollowUser(request);

        return response;
    }

    @PutMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginUserRequest request) {
        ResponseEntity<String> response = _userService.loginUser(request);

        return response;
    }

    @PutMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        ResponseEntity<String> response = _userService.logoutUser(request);

        return response;
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordUserRequest request) {
        ResponseEntity<String> response = _userService.resetPassword(request);

        return response;
    }

    @GetMapping("/my-friends")
    public ResponseEntity<?> GetFriends() {
        ResponseEntity<?> response = _userService.getMyFriends();

        return response;
    }
}
