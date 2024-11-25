package com.example.petsitter.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public boolean existsByIdAndRole(UUID id, User.UserRole role) {

        return userRepository.existsByIdAndRole(id, role);
    }

    @Override
    public Optional<UserDto> findDtoWithPasswordAndRolesByEmailAddress(String email) {

        return userRepository.findDtoWithPasswordAndRolesByEmailAddress(email);
    }
}
