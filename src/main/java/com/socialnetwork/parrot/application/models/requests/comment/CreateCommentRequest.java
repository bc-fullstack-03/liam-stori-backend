package com.socialnetwork.parrot.application.models.requests.comment;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateCommentRequest {
    public UUID idPost;
    public String content;
}
