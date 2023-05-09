package com.socialnetwork.parrot.application.models.requests.user;

import lombok.Data;
import java.util.Date;

@Data
public class UpdateUserRequest {
    public String fullName;
    public Date dateBirth;
}
