package com.example.petsitter.service.impl;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.dto.UserDTO;
import com.example.petsitter.exception.ResourceNotFoundException;
import com.example.petsitter.mapper.JobApplicationMapper;
import com.example.petsitter.mapper.JobMapper;
import com.example.petsitter.mapper.UserMapper;
import com.example.petsitter.model.User;
import com.example.petsitter.repository.UserRepository;
import com.example.petsitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;
    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;

    @Override
    public UUID registerUser(UserDTO userDTO) {

        return userRepository.save(userMapper.toUser(userDTO)).getId();
    }

    @Override
    public UserDTO viewUserWithId(UUID id) {

        return userRepository.findDtoById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional
    public UserDTO modifyUserWithId(UUID id, UserDTO userDTO) {

        return userRepository.findWithRolesById(id)
            .map(updateEntity(userDTO))
            .map(userMapper::toUserDTO)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Function<User, User> updateEntity(UserDTO userDTO) {

        return user -> {
            userMapper.updateUserFromDTO(userDTO, user);
            return user;
        };
    }

    @Override
    @Transactional
    public void deleteUserWithId(UUID id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
    }

    @Override
    public Set<JobDTO> viewJobsForUser(UUID id) {

        return userRepository.findWithJobsById(id)
            .map(User::getJobs)
            .map(jobMapper::toDTOSet)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public Set<JobApplicationDTO> viewApplicationsForUser(UUID id) {

        return userRepository.findWithApplicationsById(id)
            .map(User::getJobApplications)
            .map(jobApplicationMapper::toDTOSet)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
