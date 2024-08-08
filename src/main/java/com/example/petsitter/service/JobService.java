package com.example.petsitter.service;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JobService {

    UUID createJob(JobDTO jobDTO);
    List<JobDTO> viewAllJobs();
    JobDTO viewJobsWithId(UUID id);
    JobDTO modifyJobWithId(UUID id, JobDTO jobDTO);
    void deleteJobWithId(UUID id);
    Set<JobApplicationDTO> viewApplicationsForJob(UUID id);
    UUID createJobApplication(UUID id, JobApplicationDTO jobApplicationDTO);
}