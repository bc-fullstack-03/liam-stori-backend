package com.socialnetwork.parrot.application.models.responses.user;

import lombok.Data;

@Data
public class GetMyFriendshipResponse {
    public String photoUri;
    public String email;
    public String fullName;
}
