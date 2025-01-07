package com.example.petsitter.sessions;

import com.example.petsitter.common.Email;
import com.example.petsitter.openapi.ApiProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Users")
class SessionController {

    private final AuthenticationManager authenticationManager;

    private final JwtEncoder jwtEncoder;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start Session (Login)")
    @SecurityRequirements
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """
            {
              "email": "email@example.com",
              "password": "1Upper1Lower1Number"
            }
            """)})
    )
    @ApiResponse(responseCode = "201", description = "Session", content = @Content(
        schema = @Schema(implementation = SessionResponseDto.class), examples = {@ExampleObject(value =
            """
            {
              "user_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "auth_header": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJp..."
            }
            """
        )})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    SessionResponseDto createSession(@Valid @RequestBody SessionRequestDto sessionRequestDto) {

        if (sessionRequestDto.getEmail() == null) {
            throw new BadCredentialsException("Null email");
        }

        if (sessionRequestDto.getPassword() == null) {
            throw new BadCredentialsException("Null password");
        }

        var authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken
            .unauthenticated(sessionRequestDto.getEmail().getAddress(), sessionRequestDto.getPassword()));

        if (!(authentication.getPrincipal() instanceof SessionConfig.SecurityInfrastructureUser user)) {
            throw new RuntimeException("Unable to create Security Infrastructure User");
        }

        var userId = user.getId();

        var now = Instant.now();

        var scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        var jwtClaimsSet = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(30, ChronoUnit.MINUTES))
            .subject(String.valueOf(userId))
            .claim("scope", scope)
            .build();

        var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return new SessionResponseDto(userId, "Bearer %s"
            .formatted(jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue()));
    }
}

@Value
@Builder
@Schema(name = "SessionRequest")
class SessionRequestDto {

    @Valid
    @NotNull
    Email email;

    @NotBlank
    @Size(min=8, max=20)
    @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    String password;
}

@Schema(name = "Session")
record SessionResponseDto(

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    UUID userId,

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "apiKey (Bearer token)") String authHeader) {}
