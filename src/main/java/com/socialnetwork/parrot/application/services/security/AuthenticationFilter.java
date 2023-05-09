package com.socialnetwork.parrot.application.services.security;

import com.socialnetwork.parrot.core.entities.User;
import com.socialnetwork.parrot.core.services.interfaces.BlacklistServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.JwtServiceInterface;
import com.socialnetwork.parrot.core.services.interfaces.UserServiceInterface;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtServiceInterface _jwtService;

    @Autowired
    private UserServiceInterface _userService;

    @Autowired
    private BlacklistServiceInterface _blackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getServletPath().contains("/api/v1/user/login") || request.getServletPath().contains("/api/v1/user/create")) {
            filterChain.doFilter(request, response);
            return;
        }

        if(request.getServletPath().contains("swagger") || request.getServletPath().contains("docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("Authorization");
        String idUser = request.getHeader("RequestedBy");

        if(token == null || idUser == null || !token.startsWith("Bearer ")) {
            response.getWriter().write("User not authenticated!");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        boolean isValidToken = false;

        if(_blackListService.isInBlackList(token)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        try {
            isValidToken = _jwtService.isValidToken(token.substring(7), idUser);
        } catch (Exception e) {
            response.getWriter().write(e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if(isValidToken) {
            try {
                Optional<User> user = _userService.getUserById(UUID.fromString(idUser));

                var authentication = new UsernamePasswordAuthenticationToken(user, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.getWriter().write(e.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        } else {
            response.getWriter().write("Invalid token!");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
