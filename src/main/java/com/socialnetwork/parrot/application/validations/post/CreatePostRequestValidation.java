package com.socialnetwork.parrot.application.validations.post;

import com.socialnetwork.parrot.application.models.requests.post.CreatePostRequest;
import io.micrometer.common.util.StringUtils;

import java.util.Objects;

public class CreatePostRequestValidation {
    public boolean isValidCreatePostRequest(CreatePostRequest request) {
        return StringUtils.isNotBlank(request.title)
                && StringUtils.isNotBlank(request.content)
                && Objects.nonNull(request.postVisibility);
    }
}
