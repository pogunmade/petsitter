package com.example.petsitter.sessions;

import com.example.petsitter.users.User;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public record Session(UUID userId, Set<User.UserRole> roles) {

    public boolean hasId(UUID queryUserId) {
        return userId.equals(queryUserId);
    }

    public boolean hasRole(User.UserRole... queryRoles) {
        return Arrays.stream(queryRoles).anyMatch(roles::contains);
    }

    public boolean hasRoleAndId(User.UserRole role, UUID userId) {
        return hasId(userId) && hasRole(role);
    }

    public boolean hasRoleOrId(User.UserRole role, UUID userId) {
        return hasId(userId) || hasRole(role);
    }
}
