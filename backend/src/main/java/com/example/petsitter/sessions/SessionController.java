package com.example.petsitter.sessions;

import com.example.petsitter.common.Email;
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
class SessionController {

    private final AuthenticationManager authenticationManager;

    private final JwtEncoder jwtEncoder;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
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
class SessionRequestDto {

    @Valid
    @NotNull
    Email email;

    @NotBlank
    @Size(min=8, max=20)
    String password;
}

record SessionResponseDto(UUID userId, String authHeader) {}
