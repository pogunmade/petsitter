package com.example.petsitter.jobs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Schema(name = "JobApplication")
public class JobApplicationDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID id;

    @Builder.Default
    JobApplication.JobApplicationStatus status = JobApplication.JobApplicationStatus.PENDING;

    UUID userId;

    UUID jobId;
}
