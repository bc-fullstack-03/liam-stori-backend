package com.socialnetwork.parrot.application.services.user;

import com.socialnetwork.parrot.application.models.requests.user.*;
import com.socialnetwork.parrot.application.models.responses.user.GetMyFriendshipResponse;
import com.socialnetwork.parrot.application.validations.user.CreateUserRequestValidation;
import com.socialnetwork.parrot.application.validations.user.UpdateUserRequestValidation;
import com.socialnetwork.parrot.core.entities.Friendship;
import com.socialnetwork.parrot.core.entities.User;
import com.socialnetwork.parrot.core.enums.FriendshipStatus;
import com.socialnetwork.parrot.core.enums.UserStatus;
import com.socialnetwork.parrot.core.services.interfaces.BlacklistServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.FileUploadServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.JwtServiceInterface;
import com.socialnetwork.parrot.infrastructure.persistence.repositories.UserRepositoryInterface;
import com.socialnetwork.parrot.core.services.interfaces.UserServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserServiceInterface {
    @Autowired
    private UserRepositoryInterface _userRepository;

    @Autowired
    private JwtServiceInterface _jwtService;

    @Autowired
    private PasswordEncoder _passwordEncoder;

    @Autowired
    private FileUploadServiceInterface _fileUploadService;

    @Autowired
    private BlacklistServiceInterface _blackListService;


    @Async
    @Override
    public ResponseEntity<String> createUser(CreateUserRequest request) {
        CreateUserRequestValidation validation = new CreateUserRequestValidation();

        try {
            Optional<User> userOptional = getUserByEmail(request.email);
            if(userOptional.isPresent()) {
                return getConflictResponse("Email already exists");
            }

            if(!validation.isValidCreateUserRequest(request)) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }
            if (!validation.isValidMinimumPasswordLength(request.password)) {
                return getBadRequestResponse("Password should have at least 8 characters!");
            }

            String hash = _passwordEncoder.encode(request.password);
            User user = new User(request.fullName, request.email, hash, request.dateBirth);

            saveUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(user.getId().toString());
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while creating the user!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> updateUser(UpdateUserRequest request) {
        UpdateUserRequestValidation validation = new UpdateUserRequestValidation();

        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            if(!validation.isValidUpdateUserRequest(request)) {
                return getBadRequestResponse("Incorrect information, please try again!");
            }
            user.setFullName(request.fullName);
            user.setDateBirth(request.dateBirth);

            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while updating the user!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> updatePhotoUser(MultipartFile photo) {
        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            String photoUri = "";

            var fileName = user.getId() + "." + photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf(".") + 1);
            photoUri = _fileUploadService.upload(photo, fileName);

            user.setPhotoUri(photoUri);

            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while updating the photo!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> requestDisabledUser(UUID id) {
        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            if(!user.getId().equals(id)) {
                getUnauthorizedResponse();
            }

            user.setUserStatus(UserStatus.REQUEST_DISABLED);
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while requesting user deletion!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> disabledUser(String email, HttpServletRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }
            User user = userOptional.get();

            String authorizationHeader = request.getHeader("Authorization");
            if(authorizationHeader.isEmpty()) {
                return getUnauthorizedResponse();
            }

            if(!user.getEmail().equals(email)) {
                return getUnauthorizedResponse();
            }

            _blackListService.storageToken(authorizationHeader, user.getId());
            user.setUserStatus(UserStatus.DISABLED);
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while deleting the user!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> sendFriendshipPetitionRequest(SendFriendshipPetitionRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            Optional<User> friendPetitionEmailOptional = _userRepository.findByEmail(request.friendPetitionEmail);
            if(userOptional.isEmpty() || friendPetitionEmailOptional.isEmpty()) {
                return getNotFoundResponse("Some user does not exist!");
            }

            User user = userOptional.get();
            User friendForPetition = friendPetitionEmailOptional.get();

            if(verifyIfPetitionAlreadySent(friendForPetition, user.getId())) {
                return getConflictResponse("Friend invitation already sent!");
            }

            Friendship friendship = new Friendship(user.getId(), FriendshipStatus.PENDING);

            updateFriendshipStatusForDeclinedFriend(friendForPetition, user.getId(), friendship);

            Optional<Friendship> getPendingFriendshipForUserLogged = getPendingFriendshipForUser(user, friendForPetition.getId());
            Optional<Friendship> getPendingFriendshipForFriendForPetition = getPendingFriendshipForUser(friendForPetition, user.getId());

            if (existsPendingPetition(getPendingFriendshipForUserLogged, getPendingFriendshipForFriendForPetition)) {
                getPendingFriendshipForUserLogged.get().setStatus(FriendshipStatus.ACCEPTED);
                getPendingFriendshipForFriendForPetition.get().setStatus(FriendshipStatus.ACCEPTED);
            }

            saveUser(user);
            saveUser(friendForPetition);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while sending the friendship petition!!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> acceptedFriendshipPetitionRequest(FriendshipPetitionActionRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            Optional<User> friendAcceptedPetitionEmailOptional = _userRepository.findByEmail(request.friendPetitionEmail);
            if(userOptional.isEmpty() || friendAcceptedPetitionEmailOptional.isEmpty()) {
                return getNotFoundResponse("Some user does not exist!");
            }

            User user = userOptional.get();
            User friendAcceptedForPetition = friendAcceptedPetitionEmailOptional.get();

            Optional<Friendship> friendshipOptional = getFriendshipOptional(user, friendAcceptedForPetition.getId());

            if(friendshipOptional.isEmpty()) {
                return getNotFoundResponse("Friend request not found!");
            }

            Friendship friendship = friendshipOptional.get();
            if(verifyFriendshipStatusAccepted(friendship)) {
                return getConflictResponse("Friendship has been accepted!");
            }
            if(verifyFriendshipStatusDeclined(friendship)) {
                return getConflictResponse("Friendship has been declined!");
            }

            friendship.setStatus(FriendshipStatus.ACCEPTED);
            Friendship friendshipActualUserAlterStatus = new Friendship(user.getId(), FriendshipStatus.ACCEPTED);
            friendAcceptedForPetition.getFriendship().add(friendshipActualUserAlterStatus);

            saveUser(user);
            saveUser(friendAcceptedForPetition);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while accepting the friendship!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> declinedFriendshipPetitionRequest(FriendshipPetitionActionRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            Optional<User> friendDeclinedPetitionEmailOptional = _userRepository.findByEmail(request.friendPetitionEmail);
            if(userOptional.isEmpty() || friendDeclinedPetitionEmailOptional.isEmpty()) {
                return getNotFoundResponse("Some user does not exist!");
            }

            User user = userOptional.get();
            Optional<Friendship> friendshipOptional = getFriendshipOptional(user, friendDeclinedPetitionEmailOptional.get().getId());

            if(friendshipOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend request not found!");
            }

            Friendship friendship = friendshipOptional.get();
            if(verifyFriendshipStatusAccepted(friendship)) {
                return getConflictResponse("Friendship has been accepted!");
            }
            if(verifyFriendshipStatusDeclined(friendship)) {
                return getConflictResponse("Friendship has been declined!");
            }

            friendship.setStatus(FriendshipStatus.DECLINED);
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while denying the friendship!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> followUser(FollowActionUserRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            Optional<User> followOptional = getUserByEmail(request.emailFollow);
            if(userOptional.isEmpty() || followOptional.isEmpty()) {
                return getNotFoundResponse("Some user does not exist!");
            }

            User user = userOptional.get();
            if(user.getFollowingIds().contains(followOptional.get().getId())) {
                return getConflictResponse("You already follow this user!");
            }

            user.getFollowingIds().add(followOptional.get().getId());
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while following the user!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> unfollowUser(FollowActionUserRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            Optional<User> followOptional = getUserByEmail(request.emailFollow);
            if(userOptional.isEmpty() || followOptional.isEmpty()) {
                return getNotFoundResponse("Some user does not exist!");
            }

            User user = userOptional.get();
            if(!user.getFollowingIds().contains(followOptional.get().getId())) {
                return getConflictResponse("You do not follow this user!");
            }

            user.getFollowingIds().remove(followOptional.get().getId());
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while unfollowing the user!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> loginUser(LoginUserRequest request) {
        try {
            Optional<User> userOptional = _userRepository.findByEmail(request.email);
            if(userOptional.isEmpty()) {
                return getUnauthorizedResponse();
            }
            User user = userOptional.get();

            if(user.getUserStatus().equals(UserStatus.DISABLED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You deleted your account, contact support to get it back!");
            }

            if(!_passwordEncoder.matches(request.password, user.getPassword())) {
                return getUnauthorizedResponse();
            }

            String token = _jwtService.generateToken(user.getId());

            return ResponseEntity.status(HttpStatus.OK).body(token);
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while logging in!");
        }
    }

    @Async
    @Override
    public  ResponseEntity<String> logoutUser(HttpServletRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            User user = userOptional.get();

            String authorizationHeader = request.getHeader("Authorization");
            if(authorizationHeader.isEmpty()) {
                return getUnauthorizedResponse();
            }

            _blackListService.storageToken(authorizationHeader, user.getId());
            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while logging out!");
        }
    }

    @Async
    @Override
    public ResponseEntity<String> resetPassword(ResetPasswordUserRequest request) {
        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUnauthorizedResponse();
            }
            User user = userOptional.get();

            if(!_passwordEncoder.matches(request.currentPassword, user.getPassword())) {
                return getUnauthorizedResponse();
            }

            if(_passwordEncoder.matches(request.newPassword, user.getPassword())) {
                return getConflictResponse("The new password cannot be the same as the current password!");
            }

            user.setPassword(_passwordEncoder.encode(request.newPassword));
            saveUser(user);

            return getNoContentResponse();
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while changing the password!");
        }
    }

    @Override
    public ResponseEntity<?> getMyFriends() {
        try {
            Optional<User> userOptional = getUserRequested();
            if(userOptional.isEmpty()) {
                return getUserNotFoundResponse();
            }

            User user = userOptional.get();
            List<Friendship> friendships = user.getFriendship();

            List<GetMyFriendshipResponse> getMyFriendshipResponses = getAcceptedFriendshipResponses(friendships);

            return ResponseEntity.status(HttpStatus.OK).body(getMyFriendshipResponses);
        } catch (Exception e) {
            return getInternalServerErrorResponse("An error occurred while verifying friends!");
        }
    }

    @Override
        public Optional<User> getUserRequested() {
            return (Optional<User>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

    @Override
    public Optional<User> getUserById(UUID idUser) {
        return _userRepository.findById(idUser);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return _userRepository.findByEmail(email);
    }

    @Override
    public boolean isFollowing(UUID idUser, UUID followingUserId) {
        Optional<User> userOptional = getUserById(idUser);
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();

        List<UUID> following = user.getFollowingIds();

        return following.contains(followingUserId);
    }


    /* ------- Métodos privados -------- */

    private void saveUser(User user){
        _userRepository.save(user);
    }

    private Optional<Friendship> getFriendshipOptional(User userLogged, UUID idFriendship) {
        return userLogged.getFriendship().stream()
                .filter(f -> f.getIdFriend().equals(idFriendship))
                .findFirst();
    }

    private boolean verifyIfPetitionAlreadySent(User friendForPetition, UUID idUserLogged){
        return friendForPetition.getFriendship().stream()
                .anyMatch(f -> f.getIdFriend().equals(idUserLogged)
                        && f.getStatus().equals(FriendshipStatus.PENDING));
    }

    private void updateFriendshipStatusForDeclinedFriend(User friendForPetition, UUID idUserLogged, Friendship friendship) {
        Optional<Friendship> declinedPetitionFriendshipOptional = friendForPetition.getFriendship().stream()
                .filter(f -> f.getIdFriend().equals(idUserLogged) && f.getStatus().equals(FriendshipStatus.DECLINED))
                .findFirst();
        declinedPetitionFriendshipOptional.ifPresentOrElse(
                f -> f.setStatus(FriendshipStatus.PENDING),
                () -> friendForPetition.getFriendship().add(friendship)
        );
    }

    private Optional<Friendship> getPendingFriendshipForUser(User userLogged, UUID idFriendForPetition) {
        return userLogged.getFriendship().stream()
                .filter(f -> f.getIdFriend().equals(idFriendForPetition) && f.getStatus().equals(FriendshipStatus.PENDING))
                .findFirst();
    }

    private boolean existsPendingPetition(Optional<Friendship> friendshipUser, Optional<Friendship> friendshipForPetition) {
        return friendshipUser.isPresent() && friendshipForPetition.isPresent();
    }

    private boolean verifyFriendshipStatusAccepted(Friendship friendship){
        return friendship.getStatus() == FriendshipStatus.ACCEPTED;
    }

    private boolean verifyFriendshipStatusDeclined(Friendship friendship){
        return friendship.getStatus() == FriendshipStatus.DECLINED;
    }

    private List<GetMyFriendshipResponse> getAcceptedFriendshipResponses(List<Friendship> friendships) {
        List<GetMyFriendshipResponse> getMyFriendshipResponses = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                User friendUser = getUserById(friendship.getIdFriend()).orElse(null);
                if (friendUser != null) {
                    GetMyFriendshipResponse getMyFriendshipResponse = new GetMyFriendshipResponse();

                    getMyFriendshipResponse.setPhotoUri(friendUser.getPhotoUri());
                    getMyFriendshipResponse.setEmail(friendUser.getEmail());
                    getMyFriendshipResponse.setFullName(friendUser.getFullName());
                    getMyFriendshipResponses.add(getMyFriendshipResponse);
                }
            }
        }

        return getMyFriendshipResponses;
    }

    private ResponseEntity<String> getUserNotFoundResponse() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist!");
    }

    private ResponseEntity<String> getNotFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    private ResponseEntity<String> getUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password!");
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
