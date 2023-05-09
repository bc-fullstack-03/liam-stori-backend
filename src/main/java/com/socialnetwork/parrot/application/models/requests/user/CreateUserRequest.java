package com.socialnetwork.parrot.application.models.requests.user;

import lombok.Data;
import java.util.Date;

@Data
public class CreateUserRequest {
    public String fullName;
    public String email;
    public String password;
    public Date dateBirth;
}
