package com.socialnetwork.parrot.application.validations.commentPost;

import com.socialnetwork.parrot.application.models.requests.comment.CreateCommentRequest;
import io.micrometer.common.util.StringUtils;

public class CreateCommentPostRequestValidation {
    public boolean isValidCreateCommentRequest(CreateCommentRequest request) {
        return StringUtils.isNotBlank(request.idPost.toString())
                && StringUtils.isNotBlank(request.content);
    }
}
