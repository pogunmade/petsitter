package com.example.petsitter.service;

import com.example.petsitter.dto.JobApplicationDTO;

import java.util.UUID;

public interface JobApplicationService {

    JobApplicationDTO modifyJobApplicationWithId(UUID id, JobApplicationDTO jobApplicationDTO);
}