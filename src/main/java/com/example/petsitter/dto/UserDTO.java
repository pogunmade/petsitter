package com.example.petsitter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

public record UserDTO (

    @org.hibernate.validator.constraints.UUID
    UUID id,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password,

    @JsonProperty("full_name")
    @NotBlank
    String fullName,

    @NotEmpty
    String[] roles) {}