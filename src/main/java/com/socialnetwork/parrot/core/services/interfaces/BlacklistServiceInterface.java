package com.socialnetwork.parrot.core.services.interfaces;

import java.util.UUID;

public interface BlacklistServiceInterface {
    void storageToken(String jwt, UUID idUser);
    boolean isInBlackList(String jwt);
}
