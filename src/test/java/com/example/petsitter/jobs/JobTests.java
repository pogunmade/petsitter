package com.example.petsitter.jobs;

import com.example.petsitter.common.Email;
import com.example.petsitter.common.exception.*;
import com.example.petsitter.sessions.WithSession;
import com.example.petsitter.users.User;
import com.example.petsitter.users.UserDto;
import com.example.petsitter.users.UserTestConfig;
import com.example.petsitter.users.UserTestUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.petsitter.common.CommonConfig.DATE_TIME_FORMATTER;
import static com.example.petsitter.jobs.JobApplication.JobApplicationStatus.*;
import static com.example.petsitter.sessions.SessionTestConfig.*;
import static com.example.petsitter.users.User.UserRole.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import({JobTestConfig.class, UserTestConfig.class})
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class JobTests {

    static final UUID RANDOM_UUID = UUID.randomUUID();
    static final LocalDateTime START_TIME = LocalDateTime.now().plusWeeks(1).truncatedTo(ChronoUnit.HOURS);
    static final LocalDateTime END_TIME = LocalDateTime.now().plusWeeks(1).plusHours(1).truncatedTo(ChronoUnit.HOURS);
    static final String ACTIVITY = "Walk and house sit";
    static final JobDto.DogDto DOG_DTO = JobDto.DogDto.builder()
        .name("Lola")
        .age(5)
        .breed("Rottweiler")
        .size("40kg")
        .build();

    final JobServiceInternal jobService;

    final JobRepository jobRepository;

    final UserTestUtils userTestUtils;

    final EntityManager entityManager;

    UserDto petOwnerDto;
    UserDto petSitterDto;

    @BeforeEach
    void setupData() {

        petOwnerDto = userTestUtils.save(
            UserDto.builder()
                .email(PET_OWNER_EMAIL)
                .password("1Password!")
                .fullName("Full Name")
                .roles(Set.of(PET_OWNER))
                .build());

        petSitterDto = userTestUtils.save(
            UserDto.builder()
                .email(PET_SITTER_EMAIL)
                .password("1Password!")
                .fullName("Full Name")
                .roles(Set.of(PET_SITTER))
                .build());
    }

    @Test
    void givenNoSessionWhenCreateJobThenUnauthorizedException() {

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () -> {

            jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();
        });

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.CREATE_MSG, "Job"),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenViewAllJobsThenUnauthorizedException() {

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, jobService::viewAllJobs);

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.VIEW_MSG, "all Jobs"),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenViewJobWithIdThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        entityManager.flush();

        var jobDtoId = jobDto.getId();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->
            jobService.viewJobWithId(jobDtoId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.VIEW_MSG, "Job %s".formatted(jobDtoId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenModifyJobWithIdThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        entityManager.flush();

        var jobDtoId = jobDto.getId();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->

            jobService.modifyJobWithId(jobDtoId,

                JobDto.builder()
                    .startTime(START_TIME.plusHours(1))
                    .endTime(END_TIME.plusHours(1))
                    .build()
            )
        );

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.MODIFY_MSG, "Job %s".formatted(jobDtoId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenDeleteJobWithIdThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        var jobDtoId = jobDto.getId();

        entityManager.flush();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->
            jobService.deleteJobWithId(jobDtoId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.DELETE_MSG, "Job %s".formatted(jobDtoId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenViewApplicationsForJobThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        var jobDtoId = jobDto.getId();

        entityManager.flush();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->
            jobService.viewApplicationsForJob(jobDtoId));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.VIEW_MSG,
                "Job Applications for Job %s".formatted(jobDtoId)),
            unauthorizedException.getMessage()
        );
    }

    @Test
    void givenNoSessionWhenCreateJobApplicationThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        var jobDtoId = jobDto.getId();

        entityManager.flush();

        var jobApplicationDto = JobApplicationDto.builder()
            .status(PENDING)
            .userId(petSitterDto.getId())
            .jobId(jobDtoId)
            .build();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->
            jobService.createJobApplication(jobDtoId, jobApplicationDto));

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.CREATE_MSG,
                "Job Application for Job %s".formatted(jobDtoId)),
            unauthorizedException.getMessage()
        );
    }
    @Test
    void givenNoSessionWhenModifyJobApplicationWithIdThenUnauthorizedException() {

        var jobDto = jobRepository.save(petOwnerDto.getId(),

            JobDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build()
        );

        var jobDtoId = jobDto.getId();

        var petSitterDtoId = petSitterDto.getId();

        var jobApplicationDto = jobRepository.saveJobApplication(petSitterDtoId, jobDtoId,

            JobApplicationDto.builder()
                .status(PENDING)
                .build()
        );

        var jobApplicationDtoId = jobApplicationDto.getId();

        entityManager.flush();

        var unauthorizedException = assertThrowsExactly(UnauthorizedException.class, () ->

            jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                JobApplicationDto.builder()
                    .userId(petSitterDtoId)
                    .status(ACCEPTED)
                    .build())
        );

        assertEquals(
            UnauthorizedException.messageValueOf(UnauthorizedException.MODIFY_MSG,
                "Job Application %s".formatted(jobApplicationDtoId)),
            unauthorizedException.getMessage()
        );
    }

    @Nested
    @WithSession(PET_OWNER)
    class WithPetOwnerSessionTests {

        @Test
        void givenValidSessionWhenCreateJobWithIdThenForbiddenException() {

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .id(RANDOM_UUID)
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "Job with ID %s".formatted(RANDOM_UUID)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenValidSessionWhenCreateJobWithNullStartTimeThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job", "start_time", InvalidArgument.NULL_VALUE_MSG));
        }

        @Test
        void givenValidSessionWhenCreateJobWithNullEndTimeThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job", "end_time", InvalidArgument.NULL_VALUE_MSG));
        }

        @Test
        void givenValidSessionWhenCreateJobWithStartTimeNotBeforeEndTimeThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .endTime(START_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job",
                "start time must be before end time"));

            invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(END_TIME)
                        .endTime(START_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job",
                "start time must be before end time"));
        }

        @Test
        void givenValidSessionWhenCreateJobWithNullActivityThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job", "activity", InvalidArgument.NULL_VALUE_MSG));
        }

        @Test
        void givenValidSessionWhenCreateJobWithBlankActivityThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity("  ")
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job", "activity", InvalidArgument.BLANK_VALUE_MSG));
        }

        @Test
        void givenValidSessionWhenCreateJobWithNullDogThenInvalidArgumentException() {

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petOwnerDto.getId())
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .build()
                );

                entityManager.flush();
            });

            assertTrue(invalidArgumentException.contains("job", "dog", InvalidArgument.NULL_VALUE_MSG));
        }

        @Test
        void givenPetOwnerSessionWhenCreateJobForDifferentPetOwnerThenForbiddenException() {

            var anotherPetOwnerId = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER).getId();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () -> jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(anotherPetOwnerId)
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                )
            );

            assertEquals(
                ForbiddenException
                    .messageValueOf(ForbiddenException.CREATE_MSG, "Job for User %s".formatted(anotherPetOwnerId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenCreateJobForPetOwnerThenJobIdReturned() {

            var jobId = jobService.createJob(

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            assertFalse(jobId.toString().isBlank());
        }

        @Test
        void givenPetOwnerSessionWhenViewDifferentPetOwnerJobWithIdThenForbiddenException() {

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var jobDto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var forbiddenException =
                assertThrowsExactly(ForbiddenException.class, () -> jobService.viewJobWithId(jobDtoId));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.VIEW_MSG, "Job %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenViewPetOwnerJobWithIdThenJobReturned() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            entityManager.flush();

            assertEquals(jobDto.toBuilder().id(jobDtoId).build(), jobService.viewJobWithId(jobDtoId));
        }

        @Test
        void givenValidSessionWhenModifyJobWithIdAndModifyDtoIdNotEqualToJobIdThenForbiddenException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .id(RANDOM_UUID)
                        .endTime(END_TIME.plusHours(1))
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG, "Job ID %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenValidSessionWhenModifyJobWithIdAndStartTimeNotBeforeEndTimeThenInvalidArgumentException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var startTimePlusOneHour = START_TIME.plusHours(1);
            var startTimePlusTwoHours = START_TIME.plusHours(2);

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .startTime(startTimePlusOneHour)
                        .endTime(startTimePlusOneHour)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job",
                "start time %s must be before end time %s".formatted(startTimePlusOneHour, startTimePlusOneHour)));

            invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .startTime(startTimePlusTwoHours)
                        .endTime(startTimePlusOneHour)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job",
                "start time %s must be before end time %s".formatted(startTimePlusTwoHours, startTimePlusOneHour)));
        }

        @Test
        void givenValidSessionWhenModifyJobWithIdAndStartTimeNotBeforeCurrentEndTimeThenInvalidArgumentException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .startTime(END_TIME)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job", "start_time",
                "start time %s must be before current end time %s".formatted(
                    END_TIME.format(DATE_TIME_FORMATTER), END_TIME.format(DATE_TIME_FORMATTER))));

            var endTimePlusOneHour = END_TIME.plusHours(1);

            invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .startTime(endTimePlusOneHour)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job", "start_time",
                "start time %s must be before current end time %s".formatted(
                    endTimePlusOneHour.format(DATE_TIME_FORMATTER), END_TIME.format(DATE_TIME_FORMATTER))));
        }

        @Test
        void givenValidSessionWhenModifyJobWithIdAndEndTimeNotAfterCurrentStartTimeThenInvalidArgumentException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .endTime(START_TIME)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job", "end_time",
                "end time %s must be after current start time %s".formatted(
                    START_TIME.format(DATE_TIME_FORMATTER), START_TIME.format(DATE_TIME_FORMATTER))));

            var startTimeMinusOneMinute = START_TIME.minusMinutes(1);

            invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .endTime(startTimeMinusOneMinute)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("job", "end_time",
                "end time %s must be after current start time %s".formatted(
                    startTimeMinusOneMinute.format(DATE_TIME_FORMATTER), START_TIME.format(DATE_TIME_FORMATTER))));
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobWithIdAndCreatorUserIdModifiedThenThenForbiddenException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .creatorUserId(anotherPetOwner.getId())
                        .build()
                )
            );

            assertEquals(
                ForbiddenException
                    .messageValueOf(ForbiddenException.MODIFY_MSG, "Job creator user ID, Job %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyDifferentPetOwnerJobWithIdThenForbiddenException() {

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var jobDto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->
                jobService.modifyJobWithId(jobDtoId, jobDto)
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG, "Job %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobWithIdThenJobModified() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            entityManager.flush();

            var modifyJobDto = JobDto.builder()
                .startTime(START_TIME.plusDays(1).plusMinutes(30))
                .endTime(END_TIME.plusDays(1).plusHours(1))
                .activity("Another activity")
                .dog(JobDto.DogDto.builder()
                    .name("Minnie")
                    .age(DOG_DTO.getAge() + 1)
                    .breed("Jack Russell Terrier")
                    .size("5kg")
                    .build()
                )
                .build();

            var modifiedJobDto = jobService.modifyJobWithId(jobDtoId, modifyJobDto);

            assertEquals(modifyJobDto.toBuilder().id(jobDtoId).creatorUserId(petOwnerDto.getId()).build(),
                modifiedJobDto);
        }

        @Test
        void givenValidSessionWhenDeleteJobWithIdAndJobDoesNotExistThenNotFoundException() {

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.deleteJobWithId(RANDOM_UUID));

            assertEquals("Job %s".formatted(RANDOM_UUID), notFoundException.getMessage());
        }

        @Test
        void givenPetOwnerSessionWhenDeleteDifferentPetOwnerJobByIdThenForbiddenException() {

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var jobDto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->
                jobService.deleteJobWithId(jobDtoId));

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.DELETE_MSG, "Job %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenDeleteJobByIdThenJobDeleted() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            jobService.deleteJobWithId(jobDtoId);

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.viewJobWithId(jobDtoId));

            assertEquals("Job %s".formatted(jobDtoId), notFoundException.getMessage());
        }

        @Test
        void givenValidSessionWhenViewApplicationsForJobAndJobDoesNotExistThenNotFoundException() {

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.viewApplicationsForJob(RANDOM_UUID));

            assertEquals("Job %s".formatted(RANDOM_UUID), notFoundException.getMessage());
        }

        @Test
        void givenPetOwnerSessionWhenViewApplicationsForDifferentPetOwnerJobThenForbiddenException() {

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var jobDto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->
                jobService.viewApplicationsForJob(jobDtoId));

            assertEquals(
                ForbiddenException
                    .messageValueOf(ForbiddenException.VIEW_MSG, "Job Applications for Job %s".formatted(jobDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenViewApplicationsForPetOwnerJobThenApplicationsReturned() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplication1Dto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var petSitter2Dto = saveUser(new Email("pet-sitter2@example.com"), PET_SITTER);

            var jobApplication2Dto = jobRepository.saveJobApplication(petSitter2Dto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var jobApplicationDtoSet = jobService.viewApplicationsForJob(jobDtoId);

            assertAll(
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication1Dto)),
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication2Dto)),
                () -> assertEquals(2, jobApplicationDtoSet.size())
            );
        }

        @Test
        void givenValidSessionWhenModifyJobApplicationWithIdAndJobApplicationDoesNotExistThenNotFoundException() {

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.modifyJobApplicationWithId(RANDOM_UUID, JobApplicationDto.builder().build()));

            assertEquals("Job Application %s".formatted(RANDOM_UUID), notFoundException.getMessage());
        }

        @Test
        void givenValidSessionWhenModifyJobApplicationWithIdAndDtoIdNotEqualIdThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDto.getId(),

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var jobApplicationDtoId = jobApplicationDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .id(RANDOM_UUID)
                        .status(ACCEPTED)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "Job Application ID %s".formatted(jobApplicationDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobApplicationWithIdAndModifyStatusNotValidThenForbiddenException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var jobApplicationDtoId = jobApplicationDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .status(WITHDRAWN)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "modifying Job Application as Pet Owner, status must be in %s"
                        .formatted(List.of(ACCEPTED, PENDING, REJECTED))),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobApplicationWithIdAndUserModifiedThenForbiddenException() {

            var petOwnerDtoId = petOwnerDto.getId();

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDtoId)
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var jobApplicationDtoId = jobApplicationDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .userId(petOwnerDtoId)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "Job Application user ID. Job Application %s".formatted(jobApplicationDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobApplicationWithIdAndJobModifiedThenForbiddenException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var jobApplicationDtoId = jobApplicationDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .status(ACCEPTED)
                        .jobId(RANDOM_UUID)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "Job Application Job ID. Job Application %s".formatted(jobApplicationDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobApplicationForDifferentPetOwnerJobThenForbiddenException() {

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var jobDto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDto.getId(),

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var jobApplicationDtoId = jobApplicationDto.getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .status(ACCEPTED)
                        .build())
            );

            assertEquals(
                ForbiddenException
                    .messageValueOf(ForbiddenException.MODIFY_MSG, "Job Application %s".formatted(jobApplicationDtoId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetOwnerSessionWhenModifyJobApplicationWithIdThenJobApplicationModified() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var modifiedJobApplicationDto = jobService.modifyJobApplicationWithId(jobApplicationDto.getId(),

                JobApplicationDto.builder()
                    .status(ACCEPTED)
                    .build()
            );

            assertEquals(jobApplicationDto.toBuilder().status(ACCEPTED).build(), modifiedJobApplicationDto);
        }
    }

    @Nested
    @WithSession(PET_SITTER)
    class WithPetSitterSessionTests {

        @Test
        void givenPetSitterSessionWhenViewAllJobsThenAllJobsReturned() {

            var job1Dto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var job2Dto = jobRepository.save(anotherPetOwner.getId(),

                JobDto.builder()
                    .startTime(START_TIME.plusWeeks(1))
                    .endTime(END_TIME.plusWeeks(1).plusHours(2))
                    .activity("Walk, bath")
                    .dog(JobDto.DogDto.builder()
                        .name("Rascal")
                        .age(8)
                        .breed("Cane Corso")
                        .size("48kg")
                        .build())
                    .build()
                );

            entityManager.flush();

            var jobDtoSet = jobService.viewAllJobs();

            assertAll(
                () -> assertTrue(jobDtoSet.contains(job1Dto)),
                () -> assertTrue(jobDtoSet.contains(job2Dto)),
                () -> assertEquals(2, jobDtoSet.size())
            );
        }

        @Test
        void givenValidSessionWhenViewJobWithIdAndJobDoesNotExistThenNotFoundException() {

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.viewJobWithId(RANDOM_UUID));

            assertEquals("Job %s".formatted(RANDOM_UUID), notFoundException.getMessage());
        }

        @Test
        void givenPetSitterSessionWhenViewJobWithIdThenJobReturned() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            assertEquals(jobDto, jobService.viewJobWithId(jobDto.getId()));
        }

        @Test
        void givenValidSessionWhenCreateJobApplicationWithIdThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.createJobApplication(jobDto.getId(),

                    JobApplicationDto.builder()
                        .id(RANDOM_UUID)
                        .status(PENDING)
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "Job Application with ID %s".formatted(RANDOM_UUID)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenValidSessionWhenCreateApplicationWithNullStatusThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.createJobApplication(jobDto.getId(),

                    JobApplicationDto.builder()
                        .status(null)
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "Job Application status must be specified"),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetSitterSessionWhenCreateApplicationWithStatusNotEqualToPendingThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.createJobApplication(jobDto.getId(),

                    JobApplicationDto.builder()
                        .status(ACCEPTED)
                        .build()
                )
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "Job Application status must equal %s".formatted(PENDING)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenValidSessionWhenCreateApplicationAndDtoJobNotEqualToApplicationJobThenInvalidArgumentException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.createJobApplication(jobDtoId,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .jobId(RANDOM_UUID)
                        .build())
            );

            assertTrue(invalidArgumentException.contains("jobApplication", "job_id",
                "Job ID mismatch. If specified, Job Application Job ID must equal %s. Value specified %s"
                    .formatted(jobDtoId, RANDOM_UUID)));
        }

        @Test
        void givenValidSessionWhenCreateApplicationForJobThatDoesNotExistThenNotFoundException() {

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->

                jobService.createJobApplication(RANDOM_UUID,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .userId(petSitterDto.getId())
                        .jobId(RANDOM_UUID)
                        .build())
            );

            assertEquals("Job %s".formatted(RANDOM_UUID), notFoundException.getMessage());
        }

        @Test
        void givenValidSessionWhenCreateApplicationAndExistsApplicationForJobAndUserThenInvalidArgumentException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var jobApplicationDto = JobApplicationDto.builder()
                .status(PENDING)
                .build();

            jobService.createJobApplication(jobDtoId, jobApplicationDto);

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->
                jobService.createJobApplication(jobDtoId, jobApplicationDto));

            assertTrue(invalidArgumentException.contains("jobApplication",
                "Job applicant cannot have more than one application for the same job. Applicant %s Job %s"
                    .formatted(petSitterDto.getId(), jobDtoId)));
        }

        @Test
        void givenPetSitterSessionWhenCreateApplicationForDifferentPetSitterThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            var anotherPetSitterId = saveUser(new Email("another-pet-sitter@example.com"), PET_SITTER).getId();

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.createJobApplication(jobDtoId,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .userId(anotherPetSitterId)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "Job Application for Job %s and User %s".formatted(jobDtoId, anotherPetSitterId)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetSitterSessionWhenCreateApplicationThenIdReturned() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            entityManager.flush();

            var jobApplicationId = jobService.createJobApplication(jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .userId(petSitterDto.getId())
                    .jobId(jobDtoId)
                    .build()
            );

            assertFalse(jobApplicationId.toString().isBlank());
        }

        @Test
        void givenPetSitterSessionWhenModifyJobApplicationWithIdAndModifyStatusNotValidThenForbiddenException() {

            var jobDto = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobDtoId = jobDto.getId();

            var jobApplicationId = jobService.createJobApplication(jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationId,

                    JobApplicationDto.builder()
                        .status(ACCEPTED)
                        .build())
            );

            var validPetSitterStatusList = List.of(PENDING, WITHDRAWN);

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "modifying Job Application as Pet Sitter, status must be in %s"
                        .formatted(validPetSitterStatusList)),
                forbiddenException.getMessage()
            );

            forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationId,

                    JobApplicationDto.builder()
                        .status(REJECTED)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.MODIFY_MSG,
                    "modifying Job Application as Pet Sitter, status must be in %s"
                        .formatted(validPetSitterStatusList)),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenPetSitterSessionWhenModifyJobApplicationWithIdThenJobApplicationModified() {

            var jobDtoId = jobRepository.save(petOwnerDto.getId(),

                JobDto.builder()
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            ).getId();

            var jobApplicationId = jobService.createJobApplication(jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var modifyJobApplicationDto = JobApplicationDto.builder()
                .status(WITHDRAWN)
                .build();

            var modifiedJobApplicationDto =
                jobService.modifyJobApplicationWithId(jobApplicationId, modifyJobApplicationDto);

            assertEquals(

                modifyJobApplicationDto.toBuilder()
                    .id(jobApplicationId)
                    .userId(petSitterDto.getId())
                    .jobId(jobDtoId).build(),

                modifiedJobApplicationDto
            );
        }
    }

    @Nested
    @WithSession(ADMIN)
    class WithAdminSessionTests {

        @BeforeEach
        void setupData() {

            userTestUtils.save(

                UserDto.builder()
                    .email(ADMIN_EMAIL)
                    .password("1AdminPassword")
                    .fullName("Admin Name")
                    .roles(Set.of(ADMIN))
                    .build()
            );
        }

        @Test
        void givenAdminSessionWhenCreateJobAndCreatorUserIsNullThenForbiddenException() {

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "creating Job as administrator, creator user ID (Pet Owner) must be specified"),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenAdminSessionWhenCreateJobAndCreatorUserNotPetOwnerThenNotFoundException() {

            var petSitterId = petSitterDto.getId();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () -> {

                jobService.createJob(

                    JobDto.builder()
                        .creatorUserId(petSitterId)
                        .startTime(START_TIME)
                        .endTime(END_TIME)
                        .activity(ACTIVITY)
                        .dog(DOG_DTO)
                        .build()
                );

                entityManager.flush();
            });

            assertEquals("Pet Owner with ID %s".formatted(petSitterId), notFoundException.getMessage());
        }

        @Test
        void givenAdminSessionWhenCreateJobForPetOwnerThenJobIdReturned() {

            var jobId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            assertFalse(jobId.toString().isBlank());
        }

        @Test
        void givenAdminSessionWhenViewAllJobsThenAllJobsReturned() {

            var job1Dto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var job1DtoId = jobService.createJob(job1Dto);

            var anotherPetOwner = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER);

            var job2Dto = JobDto.builder()
                .creatorUserId(anotherPetOwner.getId())
                .startTime(START_TIME.plusWeeks(1))
                .endTime(END_TIME.plusWeeks(1).plusHours(2))
                .activity("Walk, Bath")
                .dog(JobDto.DogDto.builder()
                    .name("Rascal")
                    .age(8)
                    .breed("Cane Corso")
                    .size("48kg")
                    .build())
                .build();

            var job2DtoId = jobService.createJob(job2Dto);

            entityManager.flush();

            var jobDtoSet = jobService.viewAllJobs();

            assertAll(
                () -> assertTrue(jobDtoSet.contains(job1Dto.toBuilder().id(job1DtoId).build())),
                () -> assertTrue(jobDtoSet.contains(job2Dto.toBuilder().id(job2DtoId).build())),
                () -> assertEquals(2, jobDtoSet.size())
            );
        }

        @Test
        void givenAdminSessionWhenViewJobWithIdThenJobReturned() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            entityManager.flush();

            assertEquals(jobDto.toBuilder().id(jobDtoId).build(), jobService.viewJobWithId(jobDtoId));
        }

        @Test
        void givenAdminSessionWhenModifyJobWithIdAndCreatorUserNotPetOwnerThenNotFoundException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var petSitterId = petSitterDto.getId();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->

                jobService.modifyJobWithId(jobDtoId,

                    JobDto.builder()
                        .creatorUserId(petSitterId)
                        .build()
                )
            );

            assertEquals("Pet Owner with ID %s".formatted(petSitterId), notFoundException.getMessage());
        }

        @Test
        void givenAdminSessionWhenModifyJobWithIdAndCreatorUserModifiedThenJobModified() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            var anotherPetOwnerId = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER).getId();

            entityManager.flush();

            var modifiedJobDto = jobService.modifyJobWithId(jobDtoId,

                JobDto.builder()
                    .creatorUserId(anotherPetOwnerId)
                    .build()
            );

            assertEquals(jobDto.toBuilder().id(jobDtoId).creatorUserId(anotherPetOwnerId).build(),
                modifiedJobDto);
        }

        @Test
        void givenAdminSessionWhenModifyJobWithIdThenJobModified() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            entityManager.flush();

            var modifyJobDto = JobDto.builder()
                .startTime(START_TIME.plusDays(1).plusMinutes(30))
                .endTime(END_TIME.plusDays(1).plusHours(1))
                .activity("Another activity")
                .dog(JobDto.DogDto.builder()
                    .name("Minnie")
                    .age(DOG_DTO.getAge() + 1)
                    .breed("Jack Russell Terrier")
                    .size("5kg")
                    .build()
                )
                .build();

            var modifiedJobDto = jobService.modifyJobWithId(jobDtoId, modifyJobDto);

            assertEquals(modifyJobDto.toBuilder().id(jobDtoId).creatorUserId(petOwnerDto.getId()).build(),
                modifiedJobDto);
        }

        @Test
        void givenAdminSessionWhenDeleteJobByIdThenJobDeleted() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            jobService.deleteJobWithId(jobDtoId);

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->
                jobService.viewJobWithId(jobDtoId));

            assertEquals("Job %s".formatted(jobDtoId), notFoundException.getMessage());
        }

        @Test
        void givenAdminSessionWhenViewApplicationsForJobThenApplicationsReturned() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplication1Dto = jobRepository.saveJobApplication(petSitterDto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            var petSitter2Dto = saveUser(new Email("pet-sitter2@example.com"), PET_SITTER);

            var jobApplication2Dto = jobRepository.saveJobApplication(petSitter2Dto.getId(), jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .build()
            );

            entityManager.flush();

            var jobApplicationDtoSet = jobService.viewApplicationsForJob(jobDtoId);

            assertAll(
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication1Dto)),
                () -> assertTrue(jobApplicationDtoSet.contains(jobApplication2Dto)),
                () -> assertEquals(2, jobApplicationDtoSet.size())
            );
        }

        @Test
        void givenAdminSessionWhenCreateApplicationAndUserIsNullThenForbiddenException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var forbiddenException = assertThrowsExactly(ForbiddenException.class, () ->

                jobService.createJobApplication(jobDtoId,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .build())
            );

            assertEquals(
                ForbiddenException.messageValueOf(ForbiddenException.CREATE_MSG,
                    "creating Job Application as administrator, user ID (Pet Sitter) must be specified"),
                forbiddenException.getMessage()
            );
        }

        @Test
        void givenAdminSessionWhenCreateApplicationAndUserNotPetSitterThenNotFoundException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var anotherPetOwnerId = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER).getId();

            entityManager.flush();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->

                jobService.createJobApplication(jobDtoId,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .userId(anotherPetOwnerId)
                        .build())
            );

            assertEquals("Pet Sitter with ID %s".formatted(anotherPetOwnerId), notFoundException.getMessage());
        }

        @Test
        void givenValidSessionWhenCreateApplicationForJobAndApplicantIsJobCreatorThenInvalidArgumentException() {

            var petOwnerAndPetSitterId = saveUser(new Email("pet-owner-and-sitter@example.com"),
                PET_OWNER, PET_SITTER).getId();

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerAndPetSitterId)
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var invalidArgumentException = assertThrowsExactly(InvalidArgumentException.class, () ->

                jobService.createJobApplication(jobDtoId,

                    JobApplicationDto.builder()
                        .status(PENDING)
                        .userId(petOwnerAndPetSitterId)
                        .build()
                )
            );

            assertTrue(invalidArgumentException.contains("jobApplication",
                "Job applicant cannot be Job creator, Applicant %s Job %s"
                    .formatted(petOwnerAndPetSitterId, jobDtoId)));
        }

        @Test
        void givenAdminSessionWhenCreateApplicationThenIdReturned() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            entityManager.flush();

            var jobApplicationId = jobService.createJobApplication(jobDtoId,

                JobApplicationDto.builder()
                    .status(ACCEPTED)
                    .userId(petSitterDto.getId())
                    .jobId(jobDtoId)
                    .build()
            );

            assertFalse(jobApplicationId.toString().isBlank());
        }

        @Test
        void givenAdminSessionWhenModifyJobApplicationWithIdAndUserNotPetSitterThenNotFoundException() {

            var jobDtoId = jobService.createJob(

                JobDto.builder()
                    .creatorUserId(petOwnerDto.getId())
                    .startTime(START_TIME)
                    .endTime(END_TIME)
                    .activity(ACTIVITY)
                    .dog(DOG_DTO)
                    .build()
            );

            var jobApplicationDtoId = jobService.createJobApplication(jobDtoId,

                JobApplicationDto.builder()
                    .status(PENDING)
                    .userId(petSitterDto.getId())
                    .build()
            );

            entityManager.flush();

            var anotherPetOwnerId = saveUser(new Email("another-pet-owner@example.com"), PET_OWNER).getId();

            var notFoundException = assertThrowsExactly(NotFoundException.class, () ->

                jobService.modifyJobApplicationWithId(jobApplicationDtoId,

                    JobApplicationDto.builder()
                        .status(REJECTED)
                        .userId(anotherPetOwnerId)
                        .build()
                )
            );

            assertEquals("Pet Sitter with ID %s".formatted(anotherPetOwnerId), notFoundException.getMessage());
        }

        @Test
        void givenAdminSessionWhenModifyJobApplicationWithIdThenJobApplicationModified() {

            var jobDto = JobDto.builder()
                .creatorUserId(petOwnerDto.getId())
                .startTime(START_TIME)
                .endTime(END_TIME)
                .activity(ACTIVITY)
                .dog(DOG_DTO)
                .build();

            var jobDtoId = jobService.createJob(jobDto);

            var jobApplicationDto = JobApplicationDto.builder()
                .status(PENDING)
                .userId(petSitterDto.getId())
                .build();

            var jobApplicationDtoId = jobService.createJobApplication(jobDtoId, jobApplicationDto);

            var anotherJobDtoId = jobService.createJob(jobDto);

            var anotherPetSitterId = saveUser(new Email("another-pet-sitter@example.com"), PET_SITTER).getId();

            entityManager.flush();

            var modifyJobApplicationDto = JobApplicationDto.builder()
                .status(ACCEPTED)
                .userId(anotherPetSitterId)
                .jobId(anotherJobDtoId)
                .build();

            var modifiedJobApplicationDto =
                jobService.modifyJobApplicationWithId(jobApplicationDtoId, modifyJobApplicationDto);

            assertEquals(modifyJobApplicationDto.toBuilder().id(jobApplicationDtoId).build(),
                modifiedJobApplicationDto);
        }
    }

    UserDto saveUser(Email email, User.UserRole... roles) {

        return userTestUtils.save(
            UserDto.builder()
                .email(email)
                .password("1Password!")
                .fullName("Full Name")
                .roles(Set.of(roles))
                .build()
        );
    }
}
