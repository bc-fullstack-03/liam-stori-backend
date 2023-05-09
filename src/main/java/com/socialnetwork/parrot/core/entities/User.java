package com.socialnetwork.parrot.core.entities;

import com.socialnetwork.parrot.core.enums.UserStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String fullName;
    private String email;
    private String password;
    private Date dateBirth;
    private String photoUri;
    private UserStatus userStatus;

    private List<Friendship> friendship;
    private List<UUID> following;

    public User(String fullName, String email, String password, Date dateBirth) {
        this.setId();
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.dateBirth = dateBirth;
        this.setUserStatus(UserStatus.ACTIVATED);

        this.friendship = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    public UUID getId(){
        return this.id;
    }

    protected void setId(){
        this.id = UUID.randomUUID();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(Date dateBirth) {
        this.dateBirth = dateBirth;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public List<Friendship> getFriendship() {
        return friendship;
    }

    public void setFriendship(List<Friendship> friendship) {
        this.friendship = friendship;
    }

    public List<UUID> getFollowingIds() {
        return following;
    }

    public void setFollowingIds(List<UUID> following) {
        this.following = following;
    }
}
