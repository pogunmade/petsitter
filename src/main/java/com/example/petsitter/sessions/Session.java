package com.example.petsitter.sessions;

import com.example.petsitter.users.User;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Session(UUID userId, Set<User.UserRole> roles) {

    public Permission getPermission(Permission.Action action, Permission.Resource resource) {

        return Permissions.getPermission(action, resource, this);
    }

    public Permission getPermission(Permission.Action action, Permission.Resource resource,
                                    Map<Permission.Attribute, Object> attributes) {

        return Permissions.getPermission(action, resource, attributes, this);
    }
}
