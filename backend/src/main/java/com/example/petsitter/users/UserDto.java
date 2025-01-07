package com.example.petsitter.users;

import com.example.petsitter.common.Email;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
@Builder
@Schema(name = "User")
public class UserDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID id;

    @Valid
    Email email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 20)
    @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    String password;

    @Size(max = 50)
    String fullName;

    @Size(min = 1, max = 3)
    Set<User.UserRole> roles;
}
