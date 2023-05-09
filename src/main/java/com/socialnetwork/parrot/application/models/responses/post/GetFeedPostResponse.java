package com.socialnetwork.parrot.application.models.responses.post;

import lombok.Data;
import java.util.List;

@Data
public class GetFeedPostResponse {
    public String photoUri;
    public String author;
    public String title;
    public String content;
    public String createdAt;

    public int totalLikes;
    public int totalComments;
    public List<GetFeedCommentPostResponse> comments;
}
