package com.socialnetwork.parrot.application.validations.user;

import com.socialnetwork.parrot.application.models.requests.user.UpdateUserRequest;
import io.micrometer.common.util.StringUtils;

import java.util.Objects;

public class UpdateUserRequestValidation {
    public boolean isValidUpdateUserRequest(UpdateUserRequest request) {
        return StringUtils.isNotBlank(request.fullName)
                && Objects.nonNull(request.dateBirth);
    }
}
