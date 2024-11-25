package com.example.petsitter.jobs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.petsitter.common.CommonConfig.DATE_TIME_FORMAT;

@Value
@Builder(toBuilder = true)
@Schema(name = "Job")
public class JobDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID id;

    UUID creatorUserId;

    @Schema(type = "string", example = "2024-12-15 12:00")
    @Future
    LocalDateTime startTime;

    @Schema(type = "string", example = "2024-12-15 14:00")
    @Future
    LocalDateTime endTime;

    @Size(max=500)
    String activity;

    @Valid
    DogDto dog;

    @Value
    @Builder
    @Schema(name = "Dog")
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
