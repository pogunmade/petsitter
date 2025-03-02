package com.example.petsitter.jobs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class JobDto {

    UUID id;

    UUID creatorUserId;

    @Future
    LocalDateTime startTime;

    @Future
    LocalDateTime endTime;

    @Size(max=500)
    String activity;

    @Valid
    DogDto dog;

    @Value
    @Builder
    public static class DogDto {

        @Size(max=30)
        String name;

        @Min(0)
        @Max(50)
        Integer age;

        @Size(max=30)
        String breed;

        @Size(max=30)
        String size;
    }
}
