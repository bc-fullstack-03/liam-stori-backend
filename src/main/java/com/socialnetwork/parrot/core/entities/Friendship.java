package com.socialnetwork.parrot.core.entities;

import com.socialnetwork.parrot.core.enums.FriendshipStatus;

import java.util.UUID;

public class Friendship {
    private UUID idFriend;
    private FriendshipStatus status;

    public Friendship(UUID idFriend, FriendshipStatus status) {
        this.idFriend = idFriend;
        this.status = status;
    }

    public UUID getIdFriend() {
        return idFriend;
    }

    public void setIdFriend(UUID idFriend) {
        this.idFriend = idFriend;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }
}
