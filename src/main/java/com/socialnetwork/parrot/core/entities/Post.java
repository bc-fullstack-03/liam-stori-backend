package com.socialnetwork.parrot.core.entities;

import com.socialnetwork.parrot.core.enums.PostVisibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Post {
    private UUID id;
    private UUID idUser;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private PostVisibility postVisibility;
    private String photoUri;

    private List<String> photosUri;
    private List<UUID> likes;
    private List<CommentPost> commentPosts;

    public Post() {
        this.setId();
    }

    public Post(UUID idUser, String title, String content, PostVisibility postVisibility) {
        this.setId();
        this.idUser = idUser;
        this.title = title;
        this.content = content;
        this.setCreatedAt();
        this.postVisibility = postVisibility;

        this.likes = new ArrayList<>();
        this.commentPosts = new ArrayList<>();
    }

    public UUID getId(){
        return this.id;
    }

    protected void setId(){
        this.id = UUID.randomUUID();
    }

    public UUID getIdUser() {
        return idUser;
    }

    public void setIdUser(UUID idUser) {
        this.idUser = idUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    protected void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public PostVisibility getPostVisibility() {
        return postVisibility;
    }

    public void setPostVisibility(PostVisibility postVisibility) {
        this.postVisibility = postVisibility;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public List<String> getPhotosUri() {
        return photosUri;
    }

    public void setPhotosUri(List<String> photosUri) {
        this.photosUri = photosUri;
    }

    public List<UUID> getLikes() {
        return likes;
    }

    public void setLikes(List<UUID> likes) {
        this.likes = likes;
    }

    public List<CommentPost> getComments() {
        return commentPosts;
    }

    public void setComments(List<CommentPost> commentPosts) {
        this.commentPosts = commentPosts;
    }
}
