package com.socialnetwork.parrot.core.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentPost {
    private UUID id;
    private UUID idPost;
    private UUID idUser;
    private String content;
    private LocalDateTime createdAt;

    private List<UUID> likes;

    public CommentPost(UUID idPost, UUID idUser, String content) {
        this.setId();
        this.idPost = idPost;
        this.idUser = idUser;
        this.content = content;
        this.setCreatedAt();

        this.likes = new ArrayList<>();
    }

    public UUID getId(){
        return this.id;
    }

    protected void setId(){
        this.id = UUID.randomUUID();
    }

    public UUID getIdPost() {
        return idPost;
    }

    public void setIdPost(UUID idPost) {
        this.idPost = idPost;
    }

    public UUID getIdUser() {
        return idUser;
    }

    public void setIdUser(UUID idUser) {
        this.idUser = idUser;
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

    public List<UUID> getLikes() {
        return likes;
    }

    public void setLikes(List<UUID> likes) {
        this.likes = likes;
    }
}
