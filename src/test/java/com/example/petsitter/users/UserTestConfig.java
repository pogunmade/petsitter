package com.example.petsitter.users;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserTestConfig {

    @Bean
    UserTestUtils userTestUtils(UserRepository userRepository) {
        return new UserTestUtils(userRepository);
    }
}
