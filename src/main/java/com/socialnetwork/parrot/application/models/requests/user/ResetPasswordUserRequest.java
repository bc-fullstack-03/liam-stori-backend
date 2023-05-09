package com.socialnetwork.parrot.application.models.requests.user;

import lombok.Data;

@Data
public class ResetPasswordUserRequest {
    public String currentPassword;
    public String newPassword;
}
