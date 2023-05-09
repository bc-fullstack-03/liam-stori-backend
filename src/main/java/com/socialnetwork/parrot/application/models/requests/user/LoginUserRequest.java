package com.socialnetwork.parrot.application.models.requests.user;

import lombok.Data;

@Data
public class LoginUserRequest {
    public String email;
    public String password;
}
