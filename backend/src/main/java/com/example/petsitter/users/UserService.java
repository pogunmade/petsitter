package com.example.petsitter.users;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    boolean existsByIdAndRole(UUID id, User.UserRole role);

    Optional<UserDto> findDtoWithPasswordAndRolesByEmailAddress(String email);
}
