package com.socialnetwork.parrot.application.services.security;

import com.socialnetwork.parrot.core.entities.Blacklist;
import com.socialnetwork.parrot.core.services.interfaces.BlacklistServiceInterface;
import com.socialnetwork.parrot.infrastructure.persistence.repositories.BlacklistRepositoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BlacklistService implements BlacklistServiceInterface {
    @Autowired
    BlacklistRepositoryInterface _blackListRepository;

    public void storageToken(String jwt, UUID idUser){
        try{
            Blacklist blackList = new Blacklist(jwt, idUser);
            _blackListRepository.save(blackList);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while logging out!");
        }
    }

    public boolean isInBlackList(String jwt) {
        List<Blacklist> blacklistTokens = _blackListRepository.findAll();
        for(Blacklist token: blacklistTokens){
            if(token.getToken().equals(jwt)) {
                return true;
            }
        }
        return false;
    }
}
