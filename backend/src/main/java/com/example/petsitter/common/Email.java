package com.example.petsitter.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@Schema(type = "string", format = "email", example = "email@example.com")
public class Email {

    @JsonValue
    @JsonProperty("email")
    @jakarta.validation.constraints.Email
    String address;
}
