package com.example.petsitter.sessions;

import com.example.petsitter.common.exception.NotFoundException;
import com.example.petsitter.users.User;
import com.example.petsitter.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import static com.example.petsitter.sessions.SessionTestConfig.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = SecurityContextFactory.class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface WithSession {
    User.UserRole value();
}

class SecurityContextFactory implements WithSecurityContextFactory<WithSession> {

    private final UserService userService;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Autowired
    SecurityContextFactory(UserService userService, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {

        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public SecurityContext createSecurityContext(WithSession withSession) {

        var emailAddress = switch (withSession.value()) {
            case ADMIN -> ADMIN_EMAIL.getAddress();
            case PET_OWNER -> PET_OWNER_EMAIL.getAddress();
            case PET_SITTER -> PET_SITTER_EMAIL.getAddress();
        };

        var userDto = userService.findDtoWithPasswordAndRolesByEmailAddress(emailAddress)
            .orElseThrow(() -> new NotFoundException("User %s".formatted(emailAddress)));

        var now = Instant.now();

        var scope = userDto.getRoles().stream()
            .map(Enum::name)
            .collect(Collectors.joining(" "));

        var jwtClaimsSet = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.MINUTES))
            .subject(String.valueOf(userDto.getId()))
            .claim("scope", scope)
            .build();

        var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        var jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());

        var authenticationManager = new ProviderManager(jwtAuthenticationProvider);

        var authentication = authenticationManager.authenticate(new BearerTokenAuthenticationToken(
            jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue()));

        var securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        return securityContext;
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
