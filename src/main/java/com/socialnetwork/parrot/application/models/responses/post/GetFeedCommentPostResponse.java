package com.socialnetwork.parrot.application.models.responses.post;

import lombok.Data;

@Data
public class GetFeedCommentPostResponse {
    public String photoUri;
    public String author;
    public String content;
    public String createdAt;
    public int likes;
}
