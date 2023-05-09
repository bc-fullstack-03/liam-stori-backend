package com.socialnetwork.parrot.application.models.requests.post;

import com.socialnetwork.parrot.core.enums.PostVisibility;
import lombok.Data;

@Data
public class UpdatePostRequest {
    public String title;
    public String content;
    public PostVisibility postVisibility;
}
