package com.example.petsitter.users;

import com.example.petsitter.common.Email;
import com.example.petsitter.common.exception.*;
import com.example.petsitter.jobs.JobApplicationDto;
import com.example.petsitter.jobs.JobDto;
import com.example.petsitter.jobs.JobTestConfig;
import com.example.petsitter.jobs.JobTestUtils;
import com.example.petsitter.sessions.WithSession;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static com.example.petsitter.sessions.SessionTestConfig.*;
import static com.example.petsitter.users.User.UserRole.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({JobTestConfig.class, UserTestConfig.class})
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@Order(1)
class UserTests {

    private static final String ADMIN_PASSWORD = "1AdminPassword";
    private static final String ADMIN_FULL_NAME = "Admin Name";

    private static final UUID RANDOM_UUID = UUID.randomUUID();
    private static final Email INVALID_EMAIL = new Email("qwerty");
    private static final String VALID_PASSWORD = "1Password";
//    private static final String INVALID_PASSWORD = "password";
    private static final String VALID_FULL_NAME = "Full Name";

    private final UserServiceInternal userService;

    private final UserRepository userRepository;

    private final JobTestUtils jobTestUtils;

    private final EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    @Test
    void whenRegisterUserWithAdminRoleThenForbiddenException() {

        var forbiddenException = assertThrowsExactly(ForbiddenException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(ADMIN))
                    .build()
            );

            entityManager.flush();
        });

        assertEquals(
            ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG, "User with %s role".formatted(ADMIN)),
            forbiddenException.getMessage()
        );
    }

    @Test
    void whenRegisterUserWithIdThenForbiddenException() {

        var forbiddenException = assertThrowsExactly(ForbiddenException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .id(RANDOM_UUID)
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertEquals(
            ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG, "User with ID %s".formatted(RANDOM_UUID)),
            forbiddenException.getMessage()
        );
    }

    @Test
    void whenRegisterUserWithNullEmailThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "email", InvalidArgument.NULL_VALUE_MSG));
    }

    @Test
    void whenRegisterUserWithInvalidEmailThenConstraintViolationException() {

        var constraintViolationException = assertThrowsExactly(ConstraintViolationException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .email(INVALID_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(constraintViolationException.getConstraintViolations().stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(String::valueOf)
            .anyMatch(propertyPath -> propertyPath.equals("email")));
    }

    @Test
    void whenRegisterUserAndEmailAlreadyRegisteredThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            var userDto = UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER))
                .build();

            userService.registerUser(userDto);
            userService.registerUser(userDto);

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "email",
            "username %s already exists".formatted(PET_OWNER_EMAIL.getAddress())));
    }

    @Test
    void whenRegisterUserWithNullPasswordThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "password", InvalidArgument.NULL_VALUE_MSG));
    }

//    @Test
//    @Disabled("Disable password validation for demonstration purposes")
//    void whenRegisterUserWithInvalidPasswordThenInvalidArgumentException() {
//
//        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {
//
//            var userDto = UserDto.builder()
//                .password(INVALID_PASSWORD)
//                .email(PET_OWNER_EMAIL)
//                .fullName(VALID_FULL_NAME)
//                .roles(Set.of(PET_OWNER))
//                .build();
//
//            userService.registerUser(userDto);
//
//            entityManager.flush();
//        });
//
//        assertTrue(invalidArgumentException.contains("user", "password",
//            PasswordValidator.PATTERN_DESCRIPTION));
//    }

    @Test
    void whenRegisterUserWithNullFullNameThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .password(VALID_PASSWORD)
                    .email(PET_OWNER_EMAIL)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "full_name", InvalidArgument.NULL_VALUE_MSG));
    }

    @Test
    void whenRegisterUserWithBlankFullNameThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .password(VALID_PASSWORD)
                    .email(PET_OWNER_EMAIL)
                    .fullName("   ")
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "full_name", InvalidArgument.BLANK_VALUE_MSG));
    }

    @Test
    void whenRegisterUserWithNullRolesThenInvalidArgumentException() {

        var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

            userService.registerUser(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .build()
            );

            entityManager.flush();
        });

        assertTrue(invalidArgumentException.contains("user", "roles", InvalidArgument.NULL_VALUE_MSG));
    }

    @Test
    void whenRegisterPetOwnerThenIdReturned() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER))
                .build()
        );

        entityManager.flush();

        assertFalse(userId.toString().isBlank());
    }

    @Test
    void whenRegisterPetSitterThenIdReturned() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_SITTER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_SITTER))
                .build()
        );

        entityManager.flush();

        assertFalse(userId.toString().isBlank());
    }

    @Test
    void whenRegisterPetOwnerAndPetSitterThenIdReturned() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER, PET_SITTER))
                .build()
        );

        entityManager.flush();

        assertFalse(userId.toString().isBlank());
    }

    @Test
    void whenRegisterUserThenPasswordEncoded() {

        userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER, PET_SITTER))
                .build()
        );

        entityManager.flush();

        assertTrue(passwordEncoder.matches(VALID_PASSWORD, getPassword(PET_OWNER_EMAIL.getAddress())));
    }

    String getPassword(String emailAddress) {

        return userRepository.findDtoWithPasswordAndRolesByEmailAddress(emailAddress)
            .map(UserDto::getPassword)
            .orElseThrow(() -> new RuntimeException("Unable to retrieve User password"));
    }

    @Test
    void givenNoSessionWhenViewUserWithIdThenUnauthorizedException() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER))
                .build()
        );

        entityManager.flush();

        var unauthorizedException =
            assertThrowsExactly(UnauthorizedException.class, () -> userService.viewUserWithId(userId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.VIEW_MSG, "User %s".formatted(userId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenModifyUserWithIdThenUnauthorizedException() {

        var userDto = UserDto.builder()
            .email(PET_OWNER_EMAIL)
            .password(VALID_PASSWORD)
            .fullName(VALID_FULL_NAME)
            .roles(Set.of(PET_OWNER))
            .build();

        var userId = userService.registerUser(userDto);

        entityManager.flush();

        var unauthorizedException =
            assertThrowsExactly(UnauthorizedException.class, () -> userService.modifyUserWithId(userId, userDto));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.MODIFY_MSG, "User %s".formatted(userId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenDeleteUserWithIdThenUnauthorizedException() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER))
                .build()
        );

        entityManager.flush();

        var unauthorizedException =
            assertThrowsExactly(UnauthorizedException.class, () -> userService.deleteUserWithId(userId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.DELETE_MSG, "User %s".formatted(userId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenViewJobsForUserThenUnauthorizedException() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_OWNER))
                .build()
        );

        entityManager.flush();

        var unauthorizedException =
            assertThrowsExactly(UnauthorizedException.class, () -> userService.viewJobsForUser(userId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.VIEW_MSG, "Jobs for User %s".formatted(userId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenViewApplicationsForUserThenUnauthorizedException() {

        var userId = userService.registerUser(

            UserDto.builder()
                .email(PET_SITTER_EMAIL)
                .password(VALID_PASSWORD)
                .fullName(VALID_FULL_NAME)
                .roles(Set.of(PET_SITTER))
                .build()
        );

        entityManager.flush();

        var unauthorizedException =
            assertThrowsExactly(UnauthorizedException.class, () -> userService.viewApplicationsForUser(userId));

        assertEquals(
            UnauthorizedException
                .messageValueOf(UnauthorizedException.VIEW_MSG, "Job Applications for User %s".formatted(userId)),
            unauthorizedException.getMessage()
        );
    }

    @Nested
    @WithSession(PET_OWNER)
    class WithSessionTests {

        UserDto petOwnerDto;

        JobDto job1Dto, job2Dto;

        UserDto petSitterDto;

        JobApplicationDto jobApplication1Dto, jobApplication2Dto;

        @BeforeEach
        void setupSessionData() {

            petOwnerDto = userRepository.save(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            var petOwnerDtoId = petOwnerDto.getId();

            job1Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                    .activity("Walk")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            job2Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusWeeks(1).plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusWeeks(1).plusHours(4).truncatedTo(ChronoUnit.MINUTES))
                    .activity("House sit")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            petSitterDto = userRepository.save(

                UserDto.builder()
                    .email(PET_SITTER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_SITTER))
                    .build()
            );

            var petSitterDtoId = petSitterDto.getId();

            jobApplication1Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job1Dto.getId())
                    .build()
            );

            jobApplication2Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job2Dto.getId())
                    .build()
            );

            entityManager.flush();
        }

        @Test
        void givenSessionForUserWithIdWhenViewUserWithDifferentIdThenForbiddenException() {

            var petSitterId = petSitterDto.getId();

            var forbiddenException =
                assertThrowsExactly(ForbiddenException.class, () -> userService.viewUserWithId(petSitterId));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.VIEW_MSG, "User %s".formatted(petSitterId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenSessionForUserWithIdWhenViewUserWithIdThenUserDetailsReturned() {

            var petOwnerId = petOwnerDto.getId();

            var userDtoResponse = userService.viewUserWithId(petOwnerId);

            assertAll(
                () -> assertEquals(petOwnerId, userDtoResponse.getId()),
                () -> assertEquals(petOwnerDto.getEmail(), userDtoResponse.getEmail()),
                () -> assertEquals(petOwnerDto.getFullName(), userDtoResponse.getFullName()),
                () -> assertEquals(petOwnerDto.getRoles(), userDtoResponse.getRoles()),

                () -> assertNull(userDtoResponse.getPassword())
            );
        }

        @Test
        void givenSessionForUserWithIdWhenModifyUserWithIdAndDtoIdNotEqualToModifyIdThenForbiddenException() {

            var petOwnerId = petOwnerDto.getId();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                userService.modifyUserWithId(petOwnerId,

                    UserDto.builder()
                        .id(RANDOM_UUID)
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG, "User ID %s".formatted(petOwnerId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenSessionForUserWithIdWhenModifyUserWithIdAndModifyHasAdminRoleThenForbiddenException() {

            var petOwnerId = petOwnerDto.getId();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                userService.modifyUserWithId(petOwnerId,

                    UserDto.builder()
                        .email(new Email("updated-email@example.com"))
                        .roles(Set.of(PET_OWNER, ADMIN))
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "User %s with %s role".formatted(petOwnerId, ADMIN)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenSessionForUserIdWhenModifyUserWithIdAndModifyEmailAlreadyExistsThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                userService.modifyUserWithId(petOwnerDto.getId(),

                    UserDto.builder()
                        .email(PET_SITTER_EMAIL)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("user", "email",
                "username %s already exists".formatted(PET_SITTER_EMAIL.getAddress())));
        }

        @Test
        void givenSessionForUserWithIdWhenModifyUserWithDifferentIdThenForbiddenException() {

            var petSitterId = petSitterDto.getId();

            var forbiddenException =
                assertThrowsExactly(ForbiddenException.class, () ->
                    userService.modifyUserWithId(petSitterId, petSitterDto));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG, "User %s".formatted(petSitterId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenSessionForUserWithIdWhenModifyUserWithIdThenUserDetailsModified() {

            var petOwnerId = petOwnerDto.getId();

            var anotherEmail = new Email("another-email@example.com");
            var anotherValidPassword = "!AnotherPassword";
            var anotherValidFullName = "Another Full Name";
            var anotherSetOfRoles = Set.of(PET_OWNER, PET_SITTER);

            var userDtoResponse = userService.modifyUserWithId(petOwnerId,

                UserDto.builder()
                    .email(anotherEmail)
                    .password(anotherValidPassword)
                    .fullName(anotherValidFullName)
                    .roles(anotherSetOfRoles)
                    .build()
            );

            assertAll(
                () -> assertEquals(petOwnerId, userDtoResponse.getId()),
                () -> assertEquals(anotherEmail, userDtoResponse.getEmail()),
                () -> assertEquals(anotherValidFullName, userDtoResponse.getFullName()),
                () -> assertEquals(anotherSetOfRoles, userDtoResponse.getRoles()),

                () -> assertNull(userDtoResponse.getPassword())
            );
        }

        @Test
        void givenSessionForUserWithIdWhenModifyUserWithIdAndPasswordModifiedThenPasswordEncoded() {

            var anotherValidPassword = "!AnotherPassword";
            var anotherSetOfRoles = Set.of(PET_OWNER, PET_SITTER);

            userService.modifyUserWithId(petOwnerDto.getId(),

                UserDto.builder()
                    .password(anotherValidPassword)
                    .roles(anotherSetOfRoles)
                    .build()
            );

            entityManager.flush();

            assertTrue(passwordEncoder.matches(anotherValidPassword, getPassword(PET_OWNER_EMAIL.getAddress())));
        }

        @Test
        void givenSessionForUserWithIdWhenDeleteUserWithDifferentIdThenForbiddenException() {

            var petSitterId = petSitterDto.getId();

            var forbiddenException =
                assertThrowsExactly(ForbiddenException.class, () -> userService.deleteUserWithId(petSitterId));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.DELETE_MSG, "User %s".formatted(petSitterId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenSessionForUserWithIdWhenDeleteUserWithIdThenUserDeleted() {

            var petOwnerId = petOwnerDto.getId();

            userService.deleteUserWithId(petOwnerId);

            entityManager.flush();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                userService.viewUserWithId(petOwnerId));

            assertEquals("User %s".formatted(petOwnerId), notFoundException.getMessage());
        }

        @Test
        void givenPetOwnerSessionWhenViewJobsForDifferentPetOwnerThenForbiddenException() {

            var anotherPetOwnerDto = userRepository.save(

                UserDto.builder()
                    .email(new Email("another-pet-owner@example.com"))
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            var anotherPetOwnerId = anotherPetOwnerDto.getId();

            jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(anotherPetOwnerId)
                    .startTime(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                    .activity("Walk")
                    .dog(JobDto.DogDto.builder()
                        .name("Minnie")
                        .age(3)
                        .breed("Jack Russell Terrier")
                        .size("5kg")
                        .build())
                    .build()
            );

            var forbiddenException =
                assertThrowsExactly(ForbiddenException.class, () -> userService.viewJobsForUser(anotherPetOwnerId));

            assertEquals(
                ForbiddenException
                    .messageValueOf(ForbiddenException.VIEW_MSG, "Jobs for User %s".formatted(anotherPetOwnerId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenViewJobsForPetOwnerThenJobsReturned() {

            var jobDtoSet = userService.viewJobsForUser(petOwnerDto.getId());

            assertAll(
                () -> assertTrue(jobDtoSet.contains(job1Dto)),
                () -> assertTrue(jobDtoSet.contains(job2Dto)),
                () -> assertEquals(2, jobDtoSet.size())
            );
        }
    }

    @Nested
    @WithSession(PET_SITTER)
    class WithPetSitterSessionTests {

        UserDto petOwnerDto;

        JobDto job1Dto, job2Dto;

        UserDto petSitterDto;

        JobApplicationDto jobApplication1Dto, jobApplication2Dto;

        @BeforeEach
        void setupSessionData() {

            petOwnerDto = userRepository.save(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            var petOwnerDtoId = petOwnerDto.getId();

            job1Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                    .activity("Walk")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            job2Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusWeeks(1).plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusWeeks(1).plusHours(4).truncatedTo(ChronoUnit.MINUTES))
                    .activity("House sit")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            petSitterDto = userRepository.save(

                UserDto.builder()
                    .email(PET_SITTER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_SITTER))
                    .build()
            );

            var petSitterDtoId = petSitterDto.getId();

            jobApplication1Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job1Dto.getId())
                    .build()
            );

            jobApplication2Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job2Dto.getId())
                    .build()
            );

            entityManager.flush();
        }

        @Test
        void givenPetSitterSessionWhenViewApplicationsForDifferentPetSitterThenForbiddenException() {

            var anotherPetSitterDto = userRepository.save(

                UserDto.builder()
                    .email(new Email("another-pet-sitter@example.com"))
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_SITTER))
                    .build()
            );

            var anotherPetSitterDtoId = anotherPetSitterDto.getId();

            jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(anotherPetSitterDtoId)
                    .jobId(job1Dto.getId())
                    .build()
            );

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->
                userService.viewApplicationsForUser(anotherPetSitterDtoId));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.VIEW_MSG,
                    "Job Applications for User %s".formatted(anotherPetSitterDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetSitterSessionWhenViewApplicationsForPetSitterThenApplicationsReturned() {

            var jobApplicationDtoSet = userService.viewApplicationsForUser(petSitterDto.getId());

            assertAll(
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication1Dto)),
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication2Dto)),
                () -> assertEquals(2, jobApplicationDtoSet.size())
            );
        }
    }

    @Nested
    @WithSession(ADMIN)
    class WithAdminSessionTests {

        UserDto petOwnerDto;

        JobDto job1Dto, job2Dto;

        UserDto petSitterDto;

        JobApplicationDto jobApplication1Dto, jobApplication2Dto;

        @BeforeEach
        void setupSessionData() {

            userRepository.save(

                UserDto.builder()
                    .email(ADMIN_EMAIL)
                    .password(ADMIN_PASSWORD)
                    .fullName(ADMIN_FULL_NAME)
                    .roles(Set.of(ADMIN))
                    .build()
            );

            petOwnerDto = userRepository.save(

                UserDto.builder()
                    .email(PET_OWNER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_OWNER))
                    .build()
            );

            var petOwnerDtoId = petOwnerDto.getId();

            job1Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                    .activity("Walk")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            job2Dto = jobTestUtils.save(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(LocalDateTime.now().plusWeeks(1).plusHours(1).truncatedTo(ChronoUnit.MINUTES))
                    .endTime(LocalDateTime.now().plusWeeks(1).plusHours(4).truncatedTo(ChronoUnit.MINUTES))
                    .activity("House sit")
                    .dog(JobDto.DogDto.builder()
                        .name("Rambo")
                        .age(3)
                        .breed("Bichon Frisé")
                        .size("6kg")
                        .build())
                    .build()
            );

            petSitterDto = userRepository.save(

                UserDto.builder()
                    .email(PET_SITTER_EMAIL)
                    .password(VALID_PASSWORD)
                    .fullName(VALID_FULL_NAME)
                    .roles(Set.of(PET_SITTER))
                    .build()
            );

            var petSitterDtoId = petSitterDto.getId();

            jobApplication1Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job1Dto.getId())
                    .build()
            );

            jobApplication2Dto = jobTestUtils.saveJobApplication(

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .jobId(job2Dto.getId())
                    .build()
            );

            entityManager.flush();
        }

        @Test
        void givenAdminSessionWhenViewUserIdThenUserDetailsReturned() {

            var petOwnerId = petOwnerDto.getId();

            var userDtoResponse = userService.viewUserWithId(petOwnerId);

            assertAll(
                () -> assertEquals(petOwnerId, userDtoResponse.getId()),
                () -> assertEquals(petOwnerDto.getEmail(), userDtoResponse.getEmail()),
                () -> assertEquals(petOwnerDto.getFullName(), userDtoResponse.getFullName()),
                () -> assertEquals(petOwnerDto.getRoles(), userDtoResponse.getRoles()),

                () -> assertNull(userDtoResponse.getPassword())
            );
        }

        @Test
        void givenAdminSessionWhenModifyUserWithIdThenUserDetailsModified() {

            var petOwnerId = petOwnerDto.getId();

            var anotherEmail = new Email("another-email@exmaple.com");
            var anotherValidPassword = "!AnotherPassword";
            var anotherValidFullName = "Another Full Name";
            var anotherSetOfRoles = Set.of(PET_OWNER, PET_SITTER);

            var userDtoResponse = userService.modifyUserWithId(petOwnerId,

                UserDto.builder()
                    .email(anotherEmail)
                    .password(anotherValidPassword)
                    .fullName(anotherValidFullName)
                    .roles(anotherSetOfRoles)
                    .build()
            );

            entityManager.flush();

            assertAll(
                () -> assertEquals(petOwnerId, userDtoResponse.getId()),
                () -> assertEquals(anotherEmail, userDtoResponse.getEmail()),
                () -> assertEquals(anotherValidFullName, userDtoResponse.getFullName()),
                () -> assertEquals(anotherSetOfRoles, userDtoResponse.getRoles()),

                () -> assertNull(userDtoResponse.getPassword())
            );
        }

        @Test
        void givenAdminSessionWhenDeleteUserWithIdThenUserDeleted() {

            var petOwnerId = petOwnerDto.getId();

            userService.deleteUserWithId(petOwnerId);

            entityManager.flush();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                userService.viewUserWithId(petOwnerId));

            assertTrue(notFoundException.getMessage().contains(String.valueOf(petOwnerId)));

            var petSitterId = petSitterDto.getId();

            var petSitterResponseDto = userService.viewUserWithId(petSitterId);

            assertAll(
                () -> assertEquals(petSitterId, petSitterResponseDto.getId()),
                () -> assertEquals(petSitterDto.getEmail(), petSitterResponseDto.getEmail()),
                () -> assertEquals(petSitterDto.getFullName(), petSitterResponseDto.getFullName()),
                () -> assertEquals(petSitterDto.getRoles(), petSitterResponseDto.getRoles()),

                () -> assertNull(petSitterResponseDto.getPassword())
            );
        }

        @Test
        void givenAdminSessionWhenViewJobsForUserThenJobsReturned() {

            var jobDtoSet = userService.viewJobsForUser(petOwnerDto.getId());

            assertAll(
                () -> assertTrue(jobDtoSet.contains(job1Dto)),
                () -> assertTrue(jobDtoSet.contains(job2Dto)),
                () -> assertEquals(2, jobDtoSet.size())
            );
        }

        @Test
        void givenAdminSessionWhenViewApplicationsForUserThenApplicationsReturned() {

            var jobApplicationDtoSet = userService.viewApplicationsForUser(petSitterDto.getId());

            assertAll(
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication1Dto)),
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication2Dto)),
                () -> assertEquals(2, jobApplicationDtoSet.size())
            );
        }
    }
}
