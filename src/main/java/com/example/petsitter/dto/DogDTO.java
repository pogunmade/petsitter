package com.example.petsitter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DogDTO (

    @NotBlank
    String name,

    @Min(0)
    @Max(50)
    int age,

    @NotBlank
    String breed,

    @NotBlank
    String size) {}
