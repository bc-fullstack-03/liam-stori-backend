package com.socialnetwork.parrot.core.entities;

import java.time.LocalDateTime;
import java.util.UUID;


public class Blacklist {
    private String token;
    private UUID idUser;
    private LocalDateTime cancelHour;

    public Blacklist(String token, UUID idUser) {
        this.token = token;
        this.idUser = idUser;
        this.setCancelHour();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getIdUser() {
        return idUser;
    }

    public void setIdUser(UUID idUser) {
        this.idUser = idUser;
    }

    public LocalDateTime getCancelHour() {
        return cancelHour;
    }

    protected void setCancelHour() {
        this.cancelHour = LocalDateTime.now();
    }
}
