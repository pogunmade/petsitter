package com.example.petsitter.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class Email {

    @JsonValue
    @JsonProperty("email")
    @jakarta.validation.constraints.Email
    String address;
}
