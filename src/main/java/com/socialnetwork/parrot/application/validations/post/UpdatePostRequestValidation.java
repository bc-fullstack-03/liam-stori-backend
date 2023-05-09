package com.socialnetwork.parrot.application.validations.post;

import com.socialnetwork.parrot.application.models.requests.post.UpdatePostRequest;
import io.micrometer.common.util.StringUtils;

import java.util.Objects;

public class UpdatePostRequestValidation {
    public boolean isValidUpdatePostRequest(UpdatePostRequest request) {
        return StringUtils.isNotBlank(request.title)
                && StringUtils.isNotBlank(request.content)
                && Objects.nonNull(request.postVisibility);
    }
}
