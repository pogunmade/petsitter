package com.example.petsitter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record JobApplicationDTO (

    @org.hibernate.validator.constraints.UUID
    UUID id,

    @NotBlank
    String status,

    @JsonProperty("user_id")
    @org.hibernate.validator.constraints.UUID
    UUID userId,

    @JsonProperty("job_id")
    @org.hibernate.validator.constraints.UUID
    UUID jobId) {}