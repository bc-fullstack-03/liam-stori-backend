package com.socialnetwork.parrot.application.services.security;

import com.socialnetwork.parrot.core.services.interfaces.JwtServiceInterface;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService implements JwtServiceInterface {

    private final long EXPIRATION_TIME = 7200000;
    private final String KEY = "2646294A404E635266556A586E327235753878214125442A472D4B6150645367";

    public String generateToken(UUID idUser) {
        return Jwts
                .builder()
                .setSubject(idUser.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(genSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidToken(String token, String idUser) {
        Claims claims = Jwts.parserBuilder().setSigningKey(genSignInKey()).build().parseClaimsJws(token).getBody();

        String subject = claims.getSubject();
        Date timeExpiration = claims.getExpiration();

        return (subject.equals(idUser.toString()) && !timeExpiration.before(new Date()));
    }

    private Key genSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(KEY));
    }

    public String getToken(){
        String jwt = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");

        return jwt.substring(7);
    }
}
