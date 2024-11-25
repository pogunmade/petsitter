package com.example.petsitter.jobs;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JobTestUtils {

    private final JobRepository jobRepository;

    public JobDto save(JobDto jobDto) {
        return jobRepository.save(jobDto.getCreatorUserId(), jobDto);
    }

    public JobApplicationDto saveJobApplication(JobApplicationDto jobApplicationDto) {

        return jobRepository.saveJobApplication(jobApplicationDto.getUserId(), jobApplicationDto.getJobId(),
            jobApplicationDto);
    }
}
