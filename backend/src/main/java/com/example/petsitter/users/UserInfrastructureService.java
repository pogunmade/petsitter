package com.example.petsitter.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInfrastructureService {

    private final UserRepository userRepository;

    public User getReferenceById(UUID id) {

        return userRepository.getReferenceById(id);
    }
}
