package com.example.petsitter.sessions;

import com.example.petsitter.users.User.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    public Optional<Session> getCurrentSession() {

        if ( !(SecurityContextHolder.getContext().getAuthentication() instanceof
               JwtAuthenticationToken jwtAuthenticationToken) ) {

            return Optional.empty();
        }

        return Optional.of(new Session(
            UUID.fromString(jwtAuthenticationToken.getName()),
            jwtAuthenticationToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::valueOf)
                .collect(Collectors.toUnmodifiableSet()))
        );
    }

    public Optional<UUID> getCurrentUserId() {

        if ( !(SecurityContextHolder.getContext().getAuthentication() instanceof
            JwtAuthenticationToken jwtAuthenticationToken) ) {
            return Optional.empty();
        }

        return Optional.of(UUID.fromString(jwtAuthenticationToken.getName()));
    }
}
