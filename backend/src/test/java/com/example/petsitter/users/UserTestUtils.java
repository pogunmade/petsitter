package com.example.petsitter.users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserTestUtils {

    private final UserRepository userRepository;

    public UserDto save(UserDto userDto) {
        return userRepository.save(userDto);
    }
}
