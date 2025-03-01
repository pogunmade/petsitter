package com.example.petsitter.contracts;

import com.example.petsitter.common.Email;
import com.example.petsitter.jobs.JobApplicationDto;
import com.example.petsitter.jobs.JobDto;
import com.example.petsitter.jobs.JobTestConfig;
import com.example.petsitter.users.User;
import com.example.petsitter.users.UserDto;
import com.example.petsitter.users.UserTestConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.microcks.testcontainers.MicrocksContainer;
import io.github.microcks.testcontainers.model.Header;
import io.github.microcks.testcontainers.model.TestRequest;
import io.github.microcks.testcontainers.model.TestRunnerType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.petsitter.jobs.JobApplication.JobApplicationStatus.ACCEPTED;
import static com.example.petsitter.jobs.JobApplication.JobApplicationStatus.PENDING;
import static com.example.petsitter.users.User.UserRole.PET_OWNER;
import static com.example.petsitter.users.User.UserRole.PET_SITTER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({JobTestConfig.class, UserTestConfig.class})
@Transactional
@org.testcontainers.junit.jupiter.Testcontainers
@Order(2)
public class OpenApiTests {

    @Container
    private static MicrocksContainer microcks = new MicrocksContainer("quay.io/microcks/microcks-uber:1.11.0")
        .withMainArtifacts("openapi/pet-sitter-openapi.yaml")
        .withAccessToHost(true);

    private static final String SERVICE_ID = "Pet Sitter API:0.1",
                               RUNNER_TYPE = TestRunnerType.OPEN_API_SCHEMA.name(),
                                 END_POINT = "http://host.testcontainers.internal:%s";

    private static final UUID POST_SESSION_USER_ID = UUID.fromString("f7329e32-b689-4ded-9819-994bbe7edade"),
                                  GET_USER_USER_ID = UUID.fromString("5d33db88-ef90-4868-814c-f4dd48273a04"),
                                PATCH_USER_USER_ID = UUID.fromString("fdc09ca4-4e44-4040-8873-9eaec0b456a9"),
                               DELETE_USER_USER_ID = UUID.fromString("e3923482-3487-4327-a796-f86467f73e57"),
                             GET_USER_JOBS_USER_ID = UUID.fromString("7afc58f7-7acf-49fe-967e-9b73accc2e09"),
                 GET_USER_JOB_APPLICATIONS_USER_ID = UUID.fromString("1ccd609a-8339-40bd-b8ac-f0fbf8bea21c"),
                                  POST_JOB_USER_ID = UUID.fromString("192bfefb-d85f-42df-a655-e8281d2e7e17"),
                                  GET_JOBS_USER_ID = UUID.fromString("006242f9-f05f-43a3-a81c-991164921aad"),
                                   GET_JOB_USER_ID = UUID.fromString("17007b2c-6791-4773-bcdd-4c474698e2b9"),
                                 PATCH_JOB_USER_ID = UUID.fromString("10fba06c-8a79-4016-98af-d1c1e0039ddf"),
                                DELETE_JOB_USER_ID = UUID.fromString("3392ed39-13e7-44aa-addb-7f54cdb38a15"),
                  GET_JOB_JOB_APPLICATIONS_USER_ID = UUID.fromString("b428a589-7ad7-4005-920b-d7042bfea208"),
                      POST_JOB_APPLICATION_USER_ID = UUID.fromString("7f09a551-ee28-44eb-9507-cb0ca699628b"),
                     PATCH_JOB_APPLICATION_USER_ID = UUID.fromString("34e5ad03-1c6f-40cf-aa37-4a2a2261a92f");

    private static final String JOHN_SMITH = "John Smith",
                                  PASSWORD = "{bcrypt}$2a$10$oVTmHyAULVClvM4JZTkfa.6fzwEQeGbJrwJLbLlKZ70DV5f84iBMm";

    private static final Set<User.UserRole> SET_OF_PET_OWNER = Set.of(PET_OWNER),
                                           SET_OF_PET_SITTER = Set.of(PET_SITTER);

    private static final JobDto.DogDto

        RAMBO = JobDto.DogDto.builder()
            .name("Rambo")
            .age(3)
            .breed("Bichon Frisé")
            .size("6kg")
            .build(),

        FRANK = JobDto.DogDto.builder()
            .name("Frank")
            .age(2)
            .breed("Bulldog")
            .size("23kg")
            .build(),

        ARCHIE = JobDto.DogDto.builder()
            .name("Archie")
            .age(6)
            .breed("Bolognese")
            .size("3kg")
            .build();

    @LocalServerPort
    private Integer port;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setup() {
        Testcontainers.exposeHostPorts(port);
    }

    @Test
    void contractConformance() throws Exception {

        setupData();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestRequest testRequest = new TestRequest.Builder()
            .serviceId(SERVICE_ID)
            .runnerType(RUNNER_TYPE)
            .testEndpoint(END_POINT.formatted(port))
            .operationsHeaders(Map.ofEntries(

                Map.entry("GET /users/{uuid}", List.of(getAuthorizationHeader(GET_USER_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("PATCH /users/{uuid}",
                    List.of(getAuthorizationHeader(PATCH_USER_USER_ID, SET_OF_PET_SITTER))),

                Map.entry("DELETE /users/{uuid}",
                    List.of(getAuthorizationHeader(DELETE_USER_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("GET /users/{uuid}/jobs",
                    List.of(getAuthorizationHeader(GET_USER_JOBS_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("GET /users/{uuid}/job-applications",
                    List.of(getAuthorizationHeader(GET_USER_JOB_APPLICATIONS_USER_ID, SET_OF_PET_SITTER))),

                Map.entry("POST /jobs", List.of(getAuthorizationHeader(POST_JOB_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("GET /jobs", List.of(getAuthorizationHeader(GET_JOBS_USER_ID, SET_OF_PET_SITTER))),

                Map.entry("GET /jobs/{uuid}", List.of(getAuthorizationHeader(GET_JOB_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("PATCH /jobs/{uuid}", List.of(getAuthorizationHeader(PATCH_JOB_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("DELETE /jobs/{uuid}",
                    List.of(getAuthorizationHeader(DELETE_JOB_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("GET /jobs/{uuid}/job-applications",
                    List.of(getAuthorizationHeader(GET_JOB_JOB_APPLICATIONS_USER_ID, SET_OF_PET_OWNER))),

                Map.entry("POST /jobs/{uuid}/job-applications",
                    List.of(getAuthorizationHeader(POST_JOB_APPLICATION_USER_ID, SET_OF_PET_SITTER))),

                Map.entry("PATCH /job-applications/{uuid}",
                    List.of(getAuthorizationHeader(PATCH_JOB_APPLICATION_USER_ID, SET_OF_PET_SITTER))))
            )
            .timeout(Duration.ofSeconds(30))
            .build();

        var testResult = microcks.testEndpoint(testRequest);

        var testResultIsSuccess = testResult.isSuccess();

        if (!testResultIsSuccess) {
            ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(testResult));
        }

        assertAll(
            () -> assertTrue(testResultIsSuccess),
            () -> assertEquals(15, testResult.getTestCaseResults().size())
        );
    }

    private void setupData() {

        setupPostSessionData();
        setupGetUserData();
        setupPatchUserData();
        setupDeleteUserData();
        setupGetUserJobsData();
        setupGetUserJobApplicationsData();
        setupPostJobData();
        setupGetJobsData();
        setupGetJobData();
        setupPatchJobData();
        setupDeleteJobData();
        setupGetJobJobApplicationsData();
        setupPostJobApplicationData();
        setupPatchJobApplicationData();
    }

    private void setupPostSessionData() {

        insertUsers(

            UserDto.builder()
                .id(POST_SESSION_USER_ID)
                .email(new Email("post-session@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build()
        );
    }

    private void setupGetUserData() {

        insertUsers(

            UserDto.builder()
            .id(GET_USER_USER_ID)
            .email(new Email("get-user@example.com"))
            .fullName(JOHN_SMITH)
            .password(PASSWORD)
            .roles(SET_OF_PET_OWNER)
            .build()
        );
    }

    private void setupPatchUserData() {

        insertUsers(

            UserDto.builder()
                .id (PATCH_USER_USER_ID)
                .email(new Email("patch-user@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build()
        );
    }

    private void setupDeleteUserData() {

        insertUsers(

            UserDto.builder()
                .id(DELETE_USER_USER_ID)
                .email(new Email("delete-user@exmple.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );
    }

    private void setupGetUserJobsData() {

        insertUsers(

            UserDto.builder()
                .id(GET_USER_JOBS_USER_ID)
                .email(new Email("get-user-jobs@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(UUID.fromString("18f4d0fc-129e-4212-ad8a-c070e4619dd9"))
                .creatorUserId(GET_USER_JOBS_USER_ID)
                .startTime(LocalDateTime.of(2025, 3, 10, 12, 0))
                .endTime(LocalDateTime.of(2025, 3, 10, 14, 0))
                .activity("Walk")
                .dog(RAMBO)
                .build(),

            JobDto.builder()
                .id(UUID.fromString("d19d609a-7ab0-4abe-833b-914017be02fe"))
                .creatorUserId(GET_USER_JOBS_USER_ID)
                .startTime(LocalDateTime.of(2025, 3, 13, 12, 0))
                .endTime(LocalDateTime.of(2025, 3, 13, 17, 0))
                .activity("Walk, House sit")
                .dog(RAMBO)
                .build()
        );
    }

    private void setupGetUserJobApplicationsData() {

        var jobOwnerId = UUID.fromString("285c50a4-fc14-4c7f-bf69-cd7a6bf9c11b");
        var jobId1Id = UUID.fromString("d7c95c55-4db6-4a84-992f-6966233ea06f");
        var jobId2Id = UUID.fromString("1d7db287-1fa0-43a2-ab4b-6aa5b2bd9c56");

        insertUsers(

            UserDto.builder()
                .id(GET_USER_JOB_APPLICATIONS_USER_ID)
                .email(new Email("get-user-job-apllications@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build(),

            UserDto.builder()
                .id(jobOwnerId)
                .email(new Email("get-user-job-apllications-job-owner@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(jobId1Id)
                .creatorUserId(jobOwnerId)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Walk")
                .dog(ARCHIE)
                .build(),

            JobDto.builder()
                .id(jobId2Id)
                .creatorUserId(jobOwnerId)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Play")
                .dog(FRANK)
                .build()
        );

        insertJobApplications(

            JobApplicationDto.builder()
                .id(UUID.fromString("e60fd6aa-9386-4577-b3a7-b6d616339858"))
                .status(PENDING)
                .userId(GET_USER_JOB_APPLICATIONS_USER_ID)
                .jobId(jobId1Id)
                .build(),

            JobApplicationDto.builder()
                .id(UUID.fromString("fb141f76-cf70-417a-8967-fe2a354fbf28"))
                .status(ACCEPTED)
                .userId(GET_USER_JOB_APPLICATIONS_USER_ID)
                .jobId(jobId2Id)
                .build()
        );
    }

    private void setupPostJobData() {

        insertUsers(

            UserDto.builder()
                .id(POST_JOB_USER_ID)
                .email(new Email("post-job@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );
    }

    private void setupGetJobsData() {

        var jobOwner1Id = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        var jobOwner2Id = UUID.fromString("b191166e-70e6-4f63-b7eb-9e6e7a3ae1e5");

        var job1Id = UUID.fromString("5882fadc-50ac-432a-86f1-02b5eedd5df0");
        var job2Id = UUID.fromString("dc28aaaf-f12f-4804-b529-f1be7ec8b77d");

        insertUsers(

            UserDto.builder()
                .id(GET_JOBS_USER_ID)
                .email(new Email("get-jobs@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build(),

            UserDto.builder()
                .id(jobOwner1Id)
                .email(new Email("get-jobs-job1-owner@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build(),

            UserDto.builder()
                .id(jobOwner2Id)
                .email(new Email("get-jobs-job2-owner@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(job1Id)
                .creatorUserId(jobOwner1Id)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Play")
                .dog(RAMBO)
                .build(),

            JobDto.builder()
                .id(job2Id)
                .creatorUserId(jobOwner2Id)
                .startTime(LocalDateTime.of(2025, 3, 20, 12, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 15, 0))
                .activity("Walk")
                .dog(ARCHIE)
                .build()
        );
    }

    private void setupGetJobData() {

        insertUsers(

            UserDto.builder()
                .id(GET_JOB_USER_ID)
                .email(new Email("get-job@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(UUID.fromString("8f028c7a-b18b-4fb7-93f5-af7fa5df4de2"))
                .creatorUserId(GET_JOB_USER_ID)
                .startTime(LocalDateTime.of(2025, 3, 17, 13, 0))
                .endTime(LocalDateTime.of(2025, 3, 17, 15, 0))
                .activity("Walk, Play")
                .dog(RAMBO)
                .build()
        );
    }

    private void setupPatchJobData() {

        insertUsers(

            UserDto.builder()
                .id(PATCH_JOB_USER_ID)
                .email(new Email("patch-job@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(UUID.fromString("11eab1f0-288f-4dde-a32b-4401deb52263"))
                .creatorUserId(PATCH_JOB_USER_ID)
                .startTime(LocalDateTime.of(2028, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2028, 3, 20, 11, 30))
                .activity("Walk")
                .dog(ARCHIE)
                .build()
        );
    }

    private void setupDeleteJobData() {

        insertUsers(

            UserDto.builder()
                .id(DELETE_JOB_USER_ID)
                .email(new Email("delete-job@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(UUID.fromString("9cb2ab0a-16fb-4ead-bbde-5167c0cb823c"))
                .creatorUserId(DELETE_JOB_USER_ID)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Walk")
                .dog(FRANK)
                .build()
        );
    }

    private void setupGetJobJobApplicationsData() {

        var applicant1Id = UUID.fromString("524d82a8-5040-47bf-88c3-b75ee949be21");
        var applicant2Id = UUID.fromString("1dce6402-7bdb-4a59-a6a4-4f711af39d8d");

        var jobId = UUID.fromString("92ee8ec0-d0eb-4204-9cc6-c0696ff9e003");

        insertUsers(

            UserDto.builder()
                .id(GET_JOB_JOB_APPLICATIONS_USER_ID)
                .email(new Email("get-job-job-applications@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build(),

            UserDto.builder()
                .id(applicant1Id)
                .email(new Email("get-job-job-applications-applicant1@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build(),

            UserDto.builder()
                .id(applicant2Id)
                .email(new Email("get-job-job-applications-applicant2@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(jobId)
                .creatorUserId(GET_JOB_JOB_APPLICATIONS_USER_ID)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Walk")
                .dog(FRANK)
                .build()
        );

        insertJobApplications(

            JobApplicationDto.builder()
                .id(UUID.fromString("cfafe48a-e0d9-4999-8966-1ef532ffee57"))
                .status(PENDING)
                .userId(applicant1Id)
                .jobId(jobId)
                .build(),

            JobApplicationDto.builder()
                .id(UUID.fromString("8937e48a-dcf2-4063-ada6-cfea662f87fd"))
                .status(PENDING)
                .userId(applicant2Id)
                .jobId(jobId)
                .build()
        );
    }

    private void setupPostJobApplicationData() {

       var jobOwnerId = UUID.fromString("a1b8d59d-59b0-444e-b538-01751ba8b6ae");

        insertUsers(

            UserDto.builder()
                .id(POST_JOB_APPLICATION_USER_ID)
                .email(new Email("post-job-application@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build(),

            UserDto.builder()
                .id(jobOwnerId)
                .email(new Email("post-job-job-owner@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(UUID.fromString("5b7ab97d-c949-4ef5-8289-969dfef1916a"))
                .creatorUserId(jobOwnerId)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Walk")
                .dog(FRANK)
                .build()
        );
    }

    private void setupPatchJobApplicationData() {

        var jobOwnerId = UUID.fromString("e9f80a8c-8335-461e-9cfe-810aa171dd5d");
        var jobId = UUID.fromString("997ec585-dfd0-4b67-9166-39c3d39df501");

        insertUsers(

            UserDto.builder()
                .id(PATCH_JOB_APPLICATION_USER_ID)
                .email(new Email("patch-job-application@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_SITTER)
                .build(),

            UserDto.builder()
                .id(jobOwnerId)
                .email(new Email("patch-job-application-job-owner@example.com"))
                .fullName(JOHN_SMITH)
                .password(PASSWORD)
                .roles(SET_OF_PET_OWNER)
                .build()
        );

        insertJobs(

            JobDto.builder()
                .id(jobId)
                .creatorUserId(jobOwnerId)
                .startTime(LocalDateTime.of(2025, 3, 20, 10, 0))
                .endTime(LocalDateTime.of(2025, 3, 20, 11, 30))
                .activity("Walk")
                .dog(ARCHIE)
                .build()
        );

        insertJobApplications(

            JobApplicationDto.builder()
                .id(UUID.fromString("2c30ff89-f678-4203-ad26-df8b30e64895"))
                .status(PENDING)
                .userId(PATCH_JOB_APPLICATION_USER_ID)
                .jobId(jobId)
                .build()
        );
    }

    private void insertUsers(UserDto... users) {

        for (UserDto userDto : users) {

            entityManager.createNativeQuery("""
                INSERT INTO users (id, email, password, full_name, version)
                     VALUES (?,?,?,?,?)""")
                .setParameter(1, userDto.getId())
                .setParameter(2, userDto.getEmail().getAddress())
                .setParameter(3, userDto.getPassword())
                .setParameter(4, userDto.getFullName())
                .setParameter(5, 0)
                .executeUpdate();

            for (User.UserRole userRole : userDto.getRoles()) {

                entityManager.createNativeQuery("""
                    INSERT INTO user_roles (user_id, roles)
                         VALUES (?,?)""")
                    .setParameter(1, userDto.getId())
                    .setParameter(2, userRole.name())
                    .executeUpdate();
            }
        }
    }

    private void insertJobs(JobDto... jobs) {

        for (JobDto jobDto : jobs) {

            var dog = jobDto.getDog();

            entityManager.createNativeQuery("""
                INSERT INTO jobs (id, start_time, end_time, activity, name, age, breed, size, job_owner_id, version)
                     VALUES (?,?,?,?,?,?,?,?,?,?)""")
                .setParameter(1, jobDto.getId())
                .setParameter(2, jobDto.getStartTime())
                .setParameter(3, jobDto.getEndTime())
                .setParameter(4, jobDto.getActivity())
                .setParameter(5, dog.getName())
                .setParameter(6, dog.getAge())
                .setParameter(7, dog.getBreed())
                .setParameter(8, dog.getSize())
                .setParameter(9, jobDto.getCreatorUserId())
                .setParameter(10, 0)
                .executeUpdate();
        }
    }

    private void insertJobApplications(JobApplicationDto... jobApplications) {

        for (JobApplicationDto jobApplicationDto : jobApplications) {

            entityManager.createNativeQuery("""
                        INSERT INTO job_applications (id, application_job_id, application_owner_id, application_status,
                                                      version)
                             VALUES (?,?,?,?,?)""")
                .setParameter(1, jobApplicationDto.getId())
                .setParameter(2, jobApplicationDto.getJobId())
                .setParameter(3, jobApplicationDto.getUserId())
                .setParameter(4, jobApplicationDto.getStatus().name())
                .setParameter(5, 0)
                .executeUpdate();
        }
    }

    private Header getAuthorizationHeader(UUID userId, Set<User.UserRole> userRoles) {

        var authorizationHeader = new Header();

        authorizationHeader.setName("Authorization");
        authorizationHeader.setValues("Bearer %s".formatted(getBearerTokenValue(userId, userRoles)));

        return authorizationHeader;
    }

    private String getBearerTokenValue(UUID userId, Set<User.UserRole> roles) {

        var now = Instant.now();

        var scope = roles.stream()
            .map(Enum::name)
            .collect(Collectors.joining(" "));

        var jwtClaimSet = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.MINUTES))
            .subject(String.valueOf(userId))
            .claim("scope", scope)
            .build();

        var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimSet)).getTokenValue();
    }
}
