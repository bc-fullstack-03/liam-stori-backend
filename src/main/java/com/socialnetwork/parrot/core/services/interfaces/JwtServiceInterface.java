package com.socialnetwork.parrot.core.services.interfaces;

import java.util.UUID;

public interface JwtServiceInterface {
    String generateToken(UUID idUser);
    boolean isValidToken(String token, String idUser);
}
