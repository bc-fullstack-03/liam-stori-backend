package com.socialnetwork.parrot.application.models.requests.user;

import com.socialnetwork.parrot.core.enums.FriendshipStatus;
import lombok.Data;

@Data
public class FriendshipPetitionActionRequest {
    public FriendshipStatus status;
    public String friendPetitionEmail;
}
