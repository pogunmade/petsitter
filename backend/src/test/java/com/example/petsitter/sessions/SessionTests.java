package com.example.petsitter.sessions;

import com.example.petsitter.common.Email;
import com.example.petsitter.jobs.JobTestConfig;
import com.example.petsitter.users.UserDto;
import com.example.petsitter.users.UserTestConfig;
import com.example.petsitter.users.UserTestUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.example.petsitter.sessions.SessionTestConfig.PET_OWNER_EMAIL;
import static com.example.petsitter.users.User.UserRole.PET_OWNER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import({JobTestConfig.class, UserTestConfig.class})
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class SessionTests {

    private final static String JWT_CLAIMS_ROLES_KEY = "scope";

    private final SessionController sessionController;

    private final UserTestUtils userTestUtils;

    private final JwtDecoder jwtDecoder;

    @Test
    void whenCreateSessionWithValidCredentialsThenSessionCreated() {

        var petOwnerPassword = "1Password!";

        var userDto = userTestUtils.save(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(petOwnerPassword)
                .fullName("Full Name")
                .roles(Set.of(PET_OWNER))
                .build()
        );

        var sessionResponseDto = sessionController.createSession(

            SessionRequestDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(petOwnerPassword)
                .build()
        );

        var claims = jwtDecoder.decode(sessionResponseDto.authHeader().replaceFirst("^Bearer ", "")).getClaims();

        assertAll (
            () -> assertEquals(userDto.getId(), sessionResponseDto.userId()),

            () -> assertTrue(claims.containsKey(JWT_CLAIMS_ROLES_KEY)),
            () -> assertEquals(claims.get(JWT_CLAIMS_ROLES_KEY), PET_OWNER.name())
        );
    }

    @Test
    void whenCreateSessionWithInvalidCredentialsThenAuthenticationException() {

        var petOwnerPassword = "1Password!";

        userTestUtils.save(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(petOwnerPassword)
                .fullName("Full Name")
                .roles(Set.of(PET_OWNER))
                .build()
        );

        assertThrows(AuthenticationException.class, () ->

            sessionController.createSession(

                SessionRequestDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password("!IncorrectPassword1")
                    .build())
        );

        assertThrows(AuthenticationException.class, () ->

            sessionController.createSession(

                SessionRequestDto.builder()
                    .email(new Email("not-the-pet-owners-email@example.com"))
                    .password(petOwnerPassword)
                    .build())
        );
    }

    @Test
    void whenCreateSessionWithNullEmailThenBadCredentialsException() {

        var petOwnerPassword = "1Password!";

        userTestUtils.save(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(petOwnerPassword)
                .fullName("Full Name")
                .roles(Set.of(PET_OWNER))
                .build()
        );

        var badCredentialsException = assertThrowsExactly(BadCredentialsException.class, () ->

            sessionController.createSession(

                SessionRequestDto.builder()
                    .password(petOwnerPassword)
                    .build())
        );

        assertEquals("Null email", badCredentialsException.getMessage());
    }

    @Test
    void whenCreateSessionWithNullPasswordThenBadCredentialsException() {

        userTestUtils.save(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password("1Password!")
                .fullName("Full Name")
                .roles(Set.of(PET_OWNER))
                .build()
        );

        var badCredentialsException = assertThrowsExactly(BadCredentialsException.class, () ->

            sessionController.createSession(

                SessionRequestDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .build())
        );

        assertEquals("Null password", badCredentialsException.getMessage());
    }
}
