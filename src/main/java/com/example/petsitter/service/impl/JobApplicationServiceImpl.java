package com.example.petsitter.service.impl;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.exception.ResourceNotFoundException;
import com.example.petsitter.mapper.JobApplicationMapper;
import com.example.petsitter.model.JobApplication;
import com.example.petsitter.repository.JobApplicationRepository;
import com.example.petsitter.repository.JobRepository;
import com.example.petsitter.repository.UserRepository;
import com.example.petsitter.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    private final JobApplicationMapper jobApplicationMapper;

    @Override
    @Transactional
    public JobApplicationDTO modifyJobApplicationWithId(UUID id, JobApplicationDTO jobApplicationDTO) {

        return jobApplicationRepository.findById(id)
            .map(updateEntity(jobApplicationDTO))
            .map(jobApplicationMapper::toJobApplicationDTO)
            .orElseThrow(() -> new ResourceNotFoundException("JobApplication", id));
    }

    private Function<JobApplication, JobApplication> updateEntity(JobApplicationDTO jobApplicationDTO) {

        return jobApplication -> {
            jobApplicationMapper.updateJobApplicationFromDTO(jobApplicationDTO, jobApplication);

            UUID dtoApplicationOwnerId = jobApplicationDTO.userId();

            if (dtoApplicationOwnerId != null &&
                !jobApplication.getApplicationOwner().getId().equals(dtoApplicationOwnerId)) {

                jobApplication.setApplicationOwner(userRepository.findLazyById(dtoApplicationOwnerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", dtoApplicationOwnerId)));
            }

            UUID dtoJobId = jobApplicationDTO.jobId();

            if (dtoJobId != null && !jobApplication.getJob().getId().equals(dtoJobId)) {

                jobApplication.setJob(jobRepository.findLazyById(dtoJobId)
                    .orElseThrow(() -> new ResourceNotFoundException("Job", dtoJobId)));
            }

            return jobApplication;
        };
    }
}