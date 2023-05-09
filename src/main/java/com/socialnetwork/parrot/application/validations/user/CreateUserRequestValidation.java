package com.socialnetwork.parrot.application.validations.user;

import com.socialnetwork.parrot.application.models.requests.user.CreateUserRequest;
import io.micrometer.common.util.StringUtils;

import java.util.Objects;

public class CreateUserRequestValidation {
    public boolean isValidCreateUserRequest(CreateUserRequest request) {
        return StringUtils.isNotBlank(request.fullName)
                && StringUtils.isNotBlank(request.email)
                && StringUtils.isNotBlank(request.password)
                && Objects.nonNull(request.dateBirth);
    }

    public boolean isValidMinimumPasswordLength(String password){
        int minimumLengthAccepted = 8;

        return password.length() >= minimumLengthAccepted;
    }
}
