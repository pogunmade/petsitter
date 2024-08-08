package com.example.petsitter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobDTO (

    @org.hibernate.validator.constraints.UUID
    UUID id,

    @JsonProperty("creator_user_id")
    @org.hibernate.validator.constraints.UUID
    UUID creatorUserId,

    @JsonProperty("start_time")
    @Future
    LocalDateTime startTime,

    @JsonProperty("end_time")
    @Future
    LocalDateTime endTime,

    @NotBlank
    String activity,

    @NotNull
    DogDTO dog) {}