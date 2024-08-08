package com.example.petsitter.service.impl;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.exception.ResourceNotFoundException;
import com.example.petsitter.mapper.JobApplicationMapper;
import com.example.petsitter.mapper.JobMapper;
import com.example.petsitter.model.Job;
import com.example.petsitter.model.JobApplication;
import com.example.petsitter.model.User;
import com.example.petsitter.repository.JobApplicationRepository;
import com.example.petsitter.repository.JobRepository;
import com.example.petsitter.repository.UserRepository;
import com.example.petsitter.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;

    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;

    @Override
    public UUID createJob(JobDTO jobDTO) {

        return jobRepository.save(jobMapper.toJob(jobDTO)).getId();
    }

    @Override
    public List<JobDTO> viewAllJobs() {

        return jobRepository.findDtoAll();
    }

    @Override
    public JobDTO viewJobsWithId(UUID id) {

        return jobRepository.findDtoById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    @Override
    @Transactional
    public JobDTO modifyJobWithId(UUID id, JobDTO jobDTO) {

        return jobRepository.findById(id)
            .map(updateEntity(jobDTO))
            .map(jobMapper::toJobDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    private Function<Job, Job> updateEntity(JobDTO jobDTO) {
        return job -> {
            jobMapper.updateJobFromDTO(jobDTO, job);

            UUID dtoJobOwnerId = jobDTO.creatorUserId();

            if (dtoJobOwnerId != null && !job.getJobOwner().getId().equals(dtoJobOwnerId)) {

                job.setJobOwner(userRepository.findLazyById(dtoJobOwnerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", dtoJobOwnerId)));
            }

            return job;
        };
    }

    @Override
    @Transactional
    public void deleteJobWithId(UUID id) {

        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException("Job", id);
        }

        jobRepository.deleteById(id);
    }

    @Override
    public Set<JobApplicationDTO> viewApplicationsForJob(UUID id) {

        return jobRepository.findWithApplicationsById(id)
            .map(Job::getJobApplications)
            .map(jobApplicationMapper::toDTOSet)
            .orElseThrow(() -> new ResourceNotFoundException("Job", id));
    }

    @Override
    @Transactional
    public UUID createJobApplication(UUID jobId, JobApplicationDTO jobApplicationDTO) {

        Job job = jobRepository.findLazyById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));

        UUID userId = jobApplicationDTO.userId();

        User user = userRepository.findLazyById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return
            Optional.of(jobApplicationMapper.toJobApplication(jobApplicationDTO))
                .map(updateEntity(job, user))
                .map(jobApplicationRepository::save)
                .map(JobApplication::getId)
                .orElseThrow(() -> new RuntimeException("Unable to create job application"));
    }

    private static Function<JobApplication, JobApplication> updateEntity(Job job, User applicationOwner) {

        return jobApplication -> {
            jobApplication.setApplicationOwner(applicationOwner);
            jobApplication.setJob(job);
            return jobApplication;
        };
    }
}
