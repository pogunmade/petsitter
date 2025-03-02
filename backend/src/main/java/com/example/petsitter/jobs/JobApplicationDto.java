package com.example.petsitter.jobs;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class JobApplicationDto {

    UUID id;

    @Builder.Default
    JobApplication.JobApplicationStatus status = JobApplication.JobApplicationStatus.PENDING;

    UUID userId;

    UUID jobId;
}
