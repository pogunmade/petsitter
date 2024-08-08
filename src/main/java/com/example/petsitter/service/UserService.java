package com.example.petsitter.service;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.dto.UserDTO;

import java.util.Set;
import java.util.UUID;

public interface UserService {

    UUID registerUser(UserDTO userDTO);
    UserDTO viewUserWithId(UUID uuid);
    UserDTO modifyUserWithId(UUID uuid, UserDTO userDTO);
    void deleteUserWithId(UUID uuid);
    Set<JobDTO> viewJobsForUser(UUID uuid);
    Set<JobApplicationDTO> viewApplicationsForUser(UUID uuid);
}