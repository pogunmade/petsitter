package com.example.petsitter.jobs;

import com.example.petsitter.common.JobApplicationCollectionDto;
import com.example.petsitter.common.JobCollectionDto;
import com.example.petsitter.common.exception.*;
import com.example.petsitter.openapi.ApiProblemResponse;
import com.example.petsitter.sessions.SessionService;
import com.example.petsitter.users.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.example.petsitter.common.CommonConfig.DATE_TIME_FORMATTER;
import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON;
import static com.example.petsitter.jobs.JobApplication.JobApplicationStatus.*;
import static com.example.petsitter.users.User.UserRole.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs")
class JobController {

    private static final String EXAMPLE_JOB_ID = "5882fadc-50ac-432a-86f1-02b5eedd5df0";

    private final JobServiceInternal jobService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Job")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """
            {
              "start_time": "2024-12-02 12:00",
              "end_time": "2024-12-02 14:00",
              "activity": "Walk",
              "dog": {
                "name": "Rambo",
                "age": "3",
                "breed": "Bichon Frisé",
                "size": "6kg"
              }
            }
            """)})
    )
    @ApiResponse(responseCode = "201", description = "Created",
        headers = { @Header(name = "Location", description = "Job URI", schema = @Schema(type = "string")) })
    @ApiProblemResponse(responseCode = "400", description = "Bad Request")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    ResponseEntity<Void> createJob(@Valid @RequestBody JobDto jobDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(jobService.createJob(jobDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "View all Jobs")
    @ApiResponse(responseCode = "200", description = "All Jobs", content = @Content(
        schema = @Schema(implementation = JobCollectionDto.class), examples = {@ExampleObject(value =
            """
            {
              "items": [
                {
                  "id":  \"""" + EXAMPLE_JOB_ID + "\"," + """
                  "creator_user_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "start_time": "2024-12-02 12:00",
                  "end_time": "2024-12-02 14:00",
                  "activity": "Walk",
                  "dog": {
                    "name": "Rambo",
                    "age": "3",
                    "breed": "Bichon Frisé",
                    "size": "6kg"
                  }
                },
                {
                  "id": "dc28aaaf-f12f-4804-b529-f1be7ec8b77d",
                  "creator_user_id": "b191166e-70e6-4f63-b7eb-9e6e7a3ae1e5",
                  "start_time": "2024-12-04 13:00",
                  "end_time": "2024-12-04 17:00",
                  "activity": "Walk, House sit, Play (requires constant attention)",
                  "dog": {
                    "name": "Minnie",
                    "age": "1",
                    "breed": "Jack Russell Terrier",
                    "size": "5kg"
                  }
                }
              ]
            }
            """)}))
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    JobCollectionDto viewAllJobs() {

        return new JobCollectionDto(jobService.viewAllJobs());
    }

    @GetMapping(path = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "View Job")
    @ApiResponse(responseCode = "200", description = "Job", content = @Content(
        schema = @Schema(implementation = JobDto.class), examples = {@ExampleObject(value =
            """
            {
              "id":  \"""" + EXAMPLE_JOB_ID + "\"," + """
              "creator_user_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "start_time": "2024-12-02 12:00",
              "end_time": "2024-12-02 14:00",
              "activity": "Walk",
              "dog": {
                "name": "Rambo",
                "age": "3",
                "breed": "Bichon Frisé",
                "size": "6kg"
              }
            }
            """)}))
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Not Found")
    JobDto viewJobWithId(@Parameter(description = "Job ID") @PathVariable UUID uuid) {

        return jobService.viewJobWithId(uuid);
    }

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modify Job")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """  
            {
              "end_time": "2024-12-02 17:00",
              "activity": "Walk, Exercise",
              "dog": {
                "age": "4",
                "size": "8kg"
              }
            }
            """)})
    )
    @ApiResponse(responseCode = "200", description = "Modified Job", content = @Content(
        schema = @Schema(implementation = JobDto.class), examples = {@ExampleObject(value =
            """
            {
              "id":  \"""" + EXAMPLE_JOB_ID + "\"," + """
              "creator_user_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "start_time": "2024-12-02 12:00",
              "end_time": "2024-12-02 17:00",
              "activity": "Walk, Exercise",
              "dog": {
                "name": "Rambo",
                "age": "4",
                "breed": "Bichon Frisé",
                "size": "8kg"
              }
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "400", description = "Bad Request")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Not Found")
    JobDto modifyJobWithId(@Parameter(description = "Job ID") @PathVariable UUID uuid,
                           @Valid @RequestBody JobDto jobDTO) {

        return jobService.modifyJobWithId(uuid, jobDTO);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Job")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Not Found")
    void deleteJobWithId(@Parameter(description = "Job ID") @PathVariable UUID uuid) {

        jobService.deleteJobWithId(uuid);
    }

    @GetMapping(path = "/{uuid}/job-applications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "View Applications for Job")
    @ApiResponse(responseCode = "200", description = "Job Applications for Job", content = @Content(
        schema = @Schema(implementation = JobApplicationCollectionDto.class),  examples = {@ExampleObject(value =
            """
            {
              "items": [
                {
                  "id": "bec38ff9-4cdb-4d32-9cce-e136fa0617c1",
                  "status": "PENDING",
                  "user_id": "9a73869f-a9ee-4e13-a5c3-f48ee529ad1d",
                  "job_id": \"""" + EXAMPLE_JOB_ID + "\"" + """
                },
                {
                  "id": "45dbdc77-c25a-473c-a96a-8ce960b3b11a",
                  "status": "PENDING",
                  "user_id": "c8dec9a1-8170-4a5f-bffb-2871ada16d9e",
                  "job_id": \"""" + EXAMPLE_JOB_ID + "\"" + """
                }
              ]
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Not Found")
    JobApplicationCollectionDto viewApplicationsForJob( @Parameter(description = "Job ID") @PathVariable UUID uuid) {

        return new JobApplicationCollectionDto(jobService.viewApplicationsForJob(uuid));
    }

    @PostMapping(path = "/{uuid}/job-applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Job Application")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """
            {
              "status": "PENDING"
            }
            """)})
    )
    @ApiResponse(responseCode = "201" , description = "Created", headers =
        { @Header(name = "Location", description = "Job Application URI", schema = @Schema(type = "string")) }
    )
    @ApiProblemResponse(responseCode = "400", description = "Bad Request")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Not Found")
    ResponseEntity<Void> createJobApplication(
        @Parameter(description = "Job ID") @PathVariable UUID uuid,
        @Valid @RequestBody JobApplicationDto jobApplicationDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/job-applications/{id}")
            .buildAndExpand(jobService.createJobApplication(uuid, jobApplicationDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-applications")
@Tag(name = "Jobs")
class JobApplicationController {

    private static final String EXAMPLE_JOB_APPLICATION_ID = "45dbdc77-c25a-473c-a96a-8ce960b3b11a";

    private final JobServiceInternal jobService;

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modify Job Application")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """  
            {
              "status": "ACCEPTED"
            }
            """)})
    )
    @ApiResponse(responseCode = "200", description = "Modified Job Application", content = @Content(
        schema = @Schema(implementation = JobApplicationDto.class), examples = {@ExampleObject(value =
            """
            {
              "id":  \"""" + EXAMPLE_JOB_APPLICATION_ID + "\"," + """
              "status": "ACCEPTED",
              "user_id": "c8dec9a1-8170-4a5f-bffb-2871ada16d9e",
              "job_id": "5882fadc-50ac-432a-86f1-02b5eedd5df0"
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "Job Application Not Found")
    JobApplicationDto modifyJobApplicationWithId(
        @Parameter(description = "Job Application ID") @PathVariable UUID uuid,
        @Valid @RequestBody JobApplicationDto jobApplicationDTO) {

        return jobService.modifyJobApplicationWithId(uuid, jobApplicationDTO);
    }
}

interface JobServiceInternal {

    UUID createJob(JobDto jobDTO);

    Set<JobDto> viewAllJobs();

    JobDto viewJobWithId(UUID id);

    JobDto modifyJobWithId(UUID id, JobDto jobDTO);

    void deleteJobWithId(UUID id);

    Set<JobApplicationDto> viewApplicationsForJob(UUID id);

    UUID createJobApplication(UUID id, JobApplicationDto jobApplicationDTO);

    JobApplicationDto modifyJobApplicationWithId(UUID id, JobApplicationDto jobApplicationDTO);
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class JobServiceInternalImpl implements JobServiceInternal {

    private final UserService userService;
    private final SessionService sessionService;

    private final JobRepository jobRepository;

    @Override
    @Transactional
    public UUID createJob(JobDto jobDto) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.CREATE_MSG, "Job"));

        var currentUserIsAdmin = currentUserSession.hasRole(ADMIN);

        var currentUserIsPetOwner = currentUserSession.hasRole(PET_OWNER);

        var jobDtoCreatorUserId = jobDto.getCreatorUserId();

        var currentUserIsPetOwnerAndIsJobDtoCreatorUserId =
            currentUserIsPetOwner && currentUserSession.hasId(jobDtoCreatorUserId);

        var userId = jobDtoCreatorUserId != null ? jobDtoCreatorUserId : currentUserSession.userId();

        if ( !(currentUserIsAdmin ||
               (currentUserIsPetOwner && jobDtoCreatorUserId == null) ||
               currentUserIsPetOwnerAndIsJobDtoCreatorUserId) ) {

            throw new ForbiddenException(ForbiddenException.CREATE_MSG,
                "Job for User %s".formatted(userId));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

        var jobDtoId = jobDto.getId();

        if (jobDtoId != null) {

            invalidArgumentList.add(new InvalidArgument("job", "id",
                "cannot create Job with an ID (%s)".formatted(jobDtoId)));
        }

        var jobDtoStartTime = jobDto.getStartTime();

        if (jobDtoStartTime == null) {

            invalidArgumentList.add(new InvalidArgument("job", "start_time", InvalidArgument.NULL_VALUE_MSG));
        }

        var jobDtoEndTime = jobDto.getEndTime();

        if (jobDtoEndTime == null) {

            invalidArgumentList.add(new InvalidArgument("job", "end_time", InvalidArgument.NULL_VALUE_MSG));
        }

        if (jobDtoStartTime != null && jobDtoEndTime != null && !jobDtoStartTime.isBefore(jobDtoEndTime)) {

            invalidArgumentList.add(new InvalidArgument("job", "start time must be before end time"));
        }

        var jobDtoActivity = jobDto.getActivity();

        if (jobDtoActivity == null) {
            invalidArgumentList.add(new InvalidArgument("job", "activity", InvalidArgument.NULL_VALUE_MSG));
        }
        else if (jobDtoActivity.isBlank()) {

            invalidArgumentList.add(new InvalidArgument("job", "activity", InvalidArgument.BLANK_VALUE_MSG));
        }

        if (jobDto.getDog() == null) {
            invalidArgumentList.add(new InvalidArgument("job", "dog", InvalidArgument.NULL_VALUE_MSG));
        }

        if (currentUserIsAdmin && !currentUserIsPetOwner) {

            if (jobDtoCreatorUserId == null) {

                invalidArgumentList.add(new InvalidArgument("job", "creator_user_id",
                    "creating Job as administrator, creator user ID (Pet Owner) must be specified"));
            }
            else if (!userService.existsByIdAndRole(jobDtoCreatorUserId, PET_OWNER)) {
                throw new NotFoundException("Pet Owner with ID %s".formatted(jobDtoCreatorUserId));
            }
        }

        if (!invalidArgumentList.isEmpty()) {
            throw new InvalidArgumentException(invalidArgumentList);
        }

        return jobRepository.save(userId, jobDto).getId();
    }

    @Override
    public Set<JobDto> viewAllJobs() {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "all Jobs"));

        if (!currentUserSession.hasRole(ADMIN, PET_SITTER)) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "all Jobs");
        }

        return jobRepository.findAllDto();
    }

    @Override
    public JobDto viewJobWithId(UUID jobId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "Job %s".formatted(jobId)));

        var jobDto = jobRepository.findDtoById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        if ( !(currentUserSession.hasRole(ADMIN, PET_SITTER) ||
               currentUserSession.hasRoleAndId(PET_OWNER, jobDto.getCreatorUserId())) ) {

            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Job %s".formatted(jobId));
        }

        return jobDto;
    }

    @Override
    @Transactional
    public JobDto modifyJobWithId(UUID jobId, JobDto jobDto) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG, "Job %s".formatted(jobId)));

        var currentUserIsAdmin = currentUserSession.hasRole(ADMIN);

        var job = jobRepository.findWithJobOwnerById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        var jobOwnerId = job.getJobOwner().getId();

        if ( !(currentUserIsAdmin || currentUserSession.hasRoleAndId(PET_OWNER, jobOwnerId)) ) {
            throw new ForbiddenException(ForbiddenException.MODIFY_MSG, "Job %s".formatted(jobId));
        }

        var jobDtoId = jobDto.getId();

        if (jobDtoId != null && !jobDtoId.equals(jobId)) {
            throw new ForbiddenException(ForbiddenException.MODIFY_MSG, "Job ID %s".formatted(jobId));
        }

        var jobDtoCreatorUserId = jobDto.getCreatorUserId();

        if (jobDtoCreatorUserId != null && !jobDtoCreatorUserId.equals(jobOwnerId)) {

            if (!currentUserIsAdmin) {

                throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                    "Job creator user ID, Job %s".formatted(jobId));
            }

            if (!userService.existsByIdAndRole(jobDtoCreatorUserId, PET_OWNER)) {
                throw new NotFoundException("Pet Owner with ID %s".formatted(jobDtoCreatorUserId));
            }
        }

        var jobDtoStartTime = jobDto.getStartTime();
        var jobDtoEndTime = jobDto.getEndTime();

        if (jobDtoStartTime != null && jobDtoEndTime != null && !jobDtoStartTime.isBefore(jobDtoEndTime)) {

            throw new InvalidArgumentException("job",
                "start time %s must be before end time %s".formatted(jobDtoStartTime, jobDtoEndTime));
        }

        var jobEndTime = job.getEndTime();

        if (jobDtoStartTime != null && jobDtoEndTime == null && !jobDtoStartTime.isBefore(jobEndTime)) {

            throw new InvalidArgumentException("job", "start_time",
                "start time %s must be before current end time %s".formatted(
                    jobDtoStartTime.format(DATE_TIME_FORMATTER), jobEndTime.format(DATE_TIME_FORMATTER)));
        }

        var jobStartTime = job.getStartTime();

        if (jobDtoStartTime == null && jobDtoEndTime != null && !jobStartTime.isBefore(jobDtoEndTime)) {

            throw new InvalidArgumentException("job", "end_time",
                "end time %s must be after current start time %s".formatted(
                    jobDtoEndTime.format(DATE_TIME_FORMATTER), jobStartTime.format(DATE_TIME_FORMATTER)));
        }

        return jobRepository.updateJobFromDto(job, jobDto);
    }

    @Override
    @Transactional
    public void deleteJobWithId(UUID jobId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.DELETE_MSG, "Job %s".formatted(jobId)));

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        if ( !(currentUserSession.hasRole(ADMIN) ||
               currentUserSession.hasRoleAndId(PET_OWNER, jobOwnerId)) ) {

            throw new ForbiddenException(ForbiddenException.DELETE_MSG, "Job %s".formatted(jobId));
        }

        jobRepository.deleteById(jobId);
    }

    @Override
    public Set<JobApplicationDto> viewApplicationsForJob(UUID jobId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Job Applications for Job %s".formatted(jobId)));

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        if ( !(currentUserSession.hasRole(ADMIN) ||
               currentUserSession.hasRoleAndId(PET_OWNER, jobOwnerId)) ) {

            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Job Applications for Job %s".formatted(jobId));
        }

        return jobRepository.findAllApplicationsDtoByJobId(jobId);
    }

    @Override
    @Transactional
    public UUID createJobApplication(UUID jobId, JobApplicationDto jobApplicationDto) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.CREATE_MSG,
                "Job Application for Job %s".formatted(jobId)));

        var currentUserIsAdmin = currentUserSession.hasRole(ADMIN);

        var currentUserIsPetSitter = currentUserSession.hasRole(PET_SITTER);

        var jobApplicationDtoUserId = jobApplicationDto.getUserId();

        var currentUserIsPetSitterAndIsJobApplicationDtoUserId = currentUserIsPetSitter &&
            currentUserSession.hasId(jobApplicationDtoUserId);

        var userId = jobApplicationDtoUserId != null ? jobApplicationDtoUserId : currentUserSession.userId();

        if ( !(currentUserIsAdmin ||
               (currentUserIsPetSitter && jobApplicationDtoUserId == null) ||
               currentUserIsPetSitterAndIsJobApplicationDtoUserId) ) {

            throw new ForbiddenException(ForbiddenException.CREATE_MSG,
                "Job Application for Job %s and User %s".formatted(jobId, userId));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

        var jobApplicationDtoId = jobApplicationDto.getId();

        if (jobApplicationDtoId != null) {

            invalidArgumentList.add(new InvalidArgument("jobApplication", "id",
                "cannot create Job Application with an ID (%s)".formatted(jobApplicationDtoId)));
        }

        var jobApplicationDtoStatus = jobApplicationDto.getStatus();

        if (jobApplicationDtoStatus == null) {

            invalidArgumentList.add(new InvalidArgument("jobApplication", "status",
                InvalidArgument.NULL_VALUE_MSG));
        }
        else if (!currentUserIsAdmin && !jobApplicationDtoStatus.equals(PENDING)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication", "status",
                "Job Application status must equal %s".formatted(PENDING)));
        }

        if (currentUserIsAdmin && !currentUserIsPetSitter) {

            if (jobApplicationDtoUserId == null) {

                invalidArgumentList.add(new InvalidArgument("jobApplication", "user_id",
                    "creating Job Application as administrator, user ID (Pet Sitter) must be specified"));
            }
            else if (!userService.existsByIdAndRole(jobApplicationDtoUserId, PET_SITTER)) {
                throw new NotFoundException("Pet Sitter with ID %s".formatted(jobApplicationDtoUserId));
            }
        }

        var jobApplicationDtoJobId = jobApplicationDto.getJobId();

        if (jobApplicationDtoJobId != null && !jobApplicationDtoJobId.equals(jobId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication", "job_id",
                "Job ID mismatch. If specified, Job Application Job ID must equal %s. Value specified %s"
                    .formatted(jobApplicationDtoJobId, jobId)));
        }

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        if (jobOwnerId.equals(userId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication",
                "Job applicant cannot be Job creator, Applicant %s Job %s".formatted(userId, jobId)));
        }

        if (jobRepository.applicationExistsByJobIdAndOwnerId(jobId, userId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication",
                "Job applicant cannot have more than one application for the same job. Applicant %s Job %s"
                    .formatted(userId, jobId)));
        }

        if (!invalidArgumentList.isEmpty()) {
            throw new InvalidArgumentException(invalidArgumentList);
        }

        return jobRepository.saveJobApplication(userId, jobId, jobApplicationDto).getId();
    }

    @Override
    @Transactional
    public JobApplicationDto modifyJobApplicationWithId(UUID jobApplicationId, JobApplicationDto jobApplicationDto) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG,
                "Job Application %s".formatted(jobApplicationId)));

        var currentUserIsAdmin = currentUserSession.hasRole(ADMIN);

        var jobApplication = jobRepository.findApplicationWithOwnerAndJobById(jobApplicationId)
            .orElseThrow(() -> new NotFoundException("Job Application %s".formatted(jobApplicationId)));

        var jobApplicationOwnerId = jobApplication.getApplicationOwner().getId();

        var jobApplicationJob = jobApplication.getApplicationJob();

        var currentUserIsPetSitterAndIsJobApplicationOwner =
            currentUserSession.hasRoleAndId(PET_SITTER, jobApplicationOwnerId);

        var currentUserIsPetOwnerAndIsJobApplicationJobOwner =
            currentUserSession.hasRoleAndId(PET_OWNER, jobApplicationJob.getJobOwner().getId());

        if ( !(currentUserIsAdmin ||
               currentUserIsPetSitterAndIsJobApplicationOwner ||
               currentUserIsPetOwnerAndIsJobApplicationJobOwner) ) {

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                "Job Application %s".formatted(jobApplicationId));
        }

        var jobApplicationDtoId = jobApplicationDto.getId();

        if (jobApplicationDtoId != null && !jobApplicationDtoId.equals(jobApplicationId)) {

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                "Job Application ID %s".formatted(jobApplicationId));
        }

        var jobApplicationDtoStatus = jobApplicationDto.getStatus();

        if (jobApplicationDtoStatus != null && !currentUserIsAdmin && currentUserIsPetSitterAndIsJobApplicationOwner) {

            switch (jobApplicationDtoStatus) {
                case PENDING: case WITHDRAWN:
                    break;
                default:
                    throw new ForbiddenException(ForbiddenException.INVALID_VALUE_MSG,
                        "modifying Job Application as Pet Sitter, status must be in (%s, %s)"
                            .formatted(PENDING, WITHDRAWN));
            }
        }

        if (jobApplicationDtoStatus != null && !currentUserIsAdmin &&
            currentUserIsPetOwnerAndIsJobApplicationJobOwner) {

            switch (jobApplicationDtoStatus) {
                case ACCEPTED: case REJECTED: case PENDING:
                    break;
                default:
                    throw new ForbiddenException(ForbiddenException.INVALID_VALUE_MSG,
                        "modifying Job Application as Pet Owner, status must be in (%s, %s, %s)"
                            .formatted(ACCEPTED, REJECTED, PENDING));
            }
        }

        var jobApplicationDtoUserId = jobApplicationDto.getUserId();

        if (jobApplicationDtoUserId != null && !jobApplicationDtoUserId.equals(jobApplicationOwnerId)) {

            if (!currentUserIsAdmin) {
                throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                    "Job Application user ID. Job Application %s".formatted(jobApplicationId));
            }

            if (!userService.existsByIdAndRole(jobApplicationDtoUserId, PET_SITTER)) {
                throw new NotFoundException("Pet Sitter with ID %s".formatted(jobApplicationDtoUserId));
            }
        }

        var jobApplicationDtoJobId = jobApplicationDto.getJobId();

        if (jobApplicationDtoJobId != null && !jobApplicationDtoJobId.equals(jobApplicationJob.getId())) {

            if (!currentUserIsAdmin) {
                throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                    "Job Application Job ID. Job Application %s".formatted(jobApplicationId));
            }

            if (!jobRepository.existsById(jobApplicationDtoJobId)) {
                throw new NotFoundException("Job %s".formatted(jobApplicationDtoJobId));
            }
        }

        return jobRepository.updateJobApplicationFromDto(jobApplication, jobApplicationDto);
    }
}
