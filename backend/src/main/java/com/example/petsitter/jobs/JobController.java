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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.petsitter.common.CommonConfig.DATE_TIME_FORMATTER;
import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON;
import static com.example.petsitter.sessions.Permission.Action.*;
import static com.example.petsitter.sessions.Permission.Attribute.*;
import static com.example.petsitter.sessions.Permission.Resource.JOB;
import static com.example.petsitter.sessions.Permission.Resource.JOB_APPLICATION;
import static com.example.petsitter.users.User.UserRole.PET_OWNER;
import static com.example.petsitter.users.User.UserRole.PET_SITTER;

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

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.CREATE_MSG, "Job"));

        var jobDtoCreatorUserId = jobDto.getCreatorUserId();

        var jobOwnerId = jobDtoCreatorUserId != null ? jobDtoCreatorUserId : currentSession.userId();

        var permission = currentSession.getPermission(CREATE, JOB,

            Map.of(
                JOB_OWNER_ID_ATT, jobOwnerId,
                JOB_DTO_ATT, jobDto)
        );

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.CREATE_MSG,
                optionalReason.orElse("Job for User %s".formatted(jobOwnerId)));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

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

        if (jobDtoCreatorUserId != null &&
            !userService.existsByIdAndRole(jobDtoCreatorUserId, PET_OWNER)) {

            throw new NotFoundException("Pet Owner with ID %s".formatted(jobDtoCreatorUserId));
        }

        if (!invalidArgumentList.isEmpty()) {
            throw new InvalidArgumentException(invalidArgumentList);
        }

        return jobRepository.save(jobOwnerId, jobDto).getId();
    }

    @Override
    public Set<JobDto> viewAllJobs() {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "all Jobs"));

        var permission = currentSession.getPermission(VIEW, JOB);

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "all Jobs");
        }

        return jobRepository.findAllDto();
    }

    @Override
    public JobDto viewJobWithId(UUID jobId) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "Job %s".formatted(jobId)));

        var jobDto = jobRepository.findDtoById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        var permission = currentSession.getPermission(VIEW, JOB, Map.of(JOB_OWNER_ID_ATT, jobDto.getCreatorUserId()));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Job %s".formatted(jobId));
        }

        return jobDto;
    }

    @Override
    @Transactional
    public JobDto modifyJobWithId(UUID jobId, JobDto jobDto) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG, "Job %s".formatted(jobId)));

        var job = jobRepository.findWithJobOwnerById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        var jobOwnerId = job.getJobOwner().getId();

        var permission = currentSession.getPermission(MODIFY, JOB,

            Map.of(
                JOB_ID_ATT, job.getId(),
                JOB_OWNER_ID_ATT, jobOwnerId,
                JOB_DTO_ATT, jobDto)
        );

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                optionalReason.orElse("Job %s".formatted(jobId)));
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

        var jobDtoCreatorUserId = jobDto.getCreatorUserId();

        if (jobDtoCreatorUserId != null &&
            !jobDtoCreatorUserId.equals(jobOwnerId) &&
            !userService.existsByIdAndRole(jobDtoCreatorUserId, PET_OWNER)) {

            throw new NotFoundException("Pet Owner with ID %s".formatted(jobDtoCreatorUserId));
        }

        return jobRepository.updateJobFromDto(job, jobDto);
    }

    @Override
    @Transactional
    public void deleteJobWithId(UUID jobId) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.DELETE_MSG, "Job %s".formatted(jobId)));

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        var permission = currentSession.getPermission(DELETE, JOB, Map.of(JOB_OWNER_ID_ATT, jobOwnerId));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.DELETE_MSG, "Job %s".formatted(jobId));
        }

        jobRepository.deleteById(jobId);
    }

    @Override
    public Set<JobApplicationDto> viewApplicationsForJob(UUID jobId) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Job Applications for Job %s".formatted(jobId)));

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        var permission = currentSession.getPermission(VIEW, JOB_APPLICATION, Map.of(JOB_OWNER_ID_ATT, jobOwnerId));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Job Applications for Job %s".formatted(jobId));
        }

        return jobRepository.findAllApplicationsDtoByJobId(jobId);
    }

    @Override
    @Transactional
    public UUID createJobApplication(UUID jobId, JobApplicationDto jobApplicationDto) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.CREATE_MSG,
                "Job Application for Job %s".formatted(jobId)));

        var jobApplicationDtoUserId = jobApplicationDto.getUserId();

        var jobApplicationOwnerId =
                jobApplicationDtoUserId != null ? jobApplicationDtoUserId : currentSession.userId();

        var permission = currentSession.getPermission(CREATE, JOB_APPLICATION,

            Map.of(
                JOB_APPLICATION_OWNER_ID_ATT, jobApplicationOwnerId,
                JOB_APPLICATION_DTO_ATT, jobApplicationDto)
        );

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.CREATE_MSG,
                optionalReason.orElse("Job Application for Job %s and User %s"
                    .formatted(jobId, jobApplicationOwnerId)));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

        var jobApplicationDtoJobId = jobApplicationDto.getJobId();

        if (jobApplicationDtoJobId != null && !jobApplicationDtoJobId.equals(jobId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication", "job_id",
                "Job ID mismatch. If specified, Job Application Job ID must equal %s. Value specified %s"
                    .formatted(jobId, jobApplicationDtoJobId)));
        }

        if (jobApplicationDtoUserId != null &&
            !userService.existsByIdAndRole(jobApplicationDtoUserId, PET_SITTER)) {

            throw new NotFoundException("Pet Sitter with ID %s".formatted(jobApplicationDtoUserId));
        }

        var jobOwnerId = jobRepository.findJobOwnerIdById(jobId)
            .orElseThrow(() -> new NotFoundException("Job %s".formatted(jobId)));

        if (jobOwnerId.equals(jobApplicationOwnerId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication",
                "Job applicant cannot be Job creator, Applicant %s Job %s".formatted(jobApplicationOwnerId, jobId)));
        }

        if (jobRepository.applicationExistsByJobIdAndOwnerId(jobId, jobApplicationOwnerId)) {

            invalidArgumentList.add(new InvalidArgument("jobApplication",
                "Job applicant cannot have more than one application for the same job. Applicant %s Job %s"
                    .formatted(jobApplicationOwnerId, jobId)));
        }

        if (!invalidArgumentList.isEmpty()) {
            throw new InvalidArgumentException(invalidArgumentList);
        }

        return jobRepository.saveJobApplication(jobApplicationOwnerId, jobId, jobApplicationDto).getId();
    }

    @Override
    @Transactional
    public JobApplicationDto modifyJobApplicationWithId(UUID jobApplicationId, JobApplicationDto jobApplicationDto) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG,
                "Job Application %s".formatted(jobApplicationId)));

        var jobApplication = jobRepository.findApplicationWithOwnerAndJobById(jobApplicationId)
            .orElseThrow(() -> new NotFoundException("Job Application %s".formatted(jobApplicationId)));

        var permission = currentSession.getPermission(MODIFY, JOB_APPLICATION,

            Map.of(
                JOB_APPLICATION_ATT, jobApplication,
                JOB_APPLICATION_DTO_ATT, jobApplicationDto)
        );

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                optionalReason.orElse("Job Application %s".formatted(jobApplicationId)));
        }

        var jobApplicationDtoUserId = jobApplicationDto.getUserId();
        var jobApplicationOwnerId = jobApplication.getApplicationOwner().getId();

        if (jobApplicationDtoUserId != null &&
            !jobApplicationDtoUserId.equals(jobApplicationOwnerId) &&
            !userService.existsByIdAndRole(jobApplicationDtoUserId, PET_SITTER)) {

            throw new NotFoundException("Pet Sitter with ID %s".formatted(jobApplicationDtoUserId));
        }

        var jobApplicationDtoJobId = jobApplicationDto.getJobId();
        var jobApplicationJobId = jobApplication.getApplicationJob().getId();

        if(jobApplicationDtoJobId != null &&
           !jobApplicationDtoJobId.equals(jobApplicationJobId) &&
           !jobRepository.existsById(jobApplicationDtoJobId)) {

            throw new NotFoundException("Job %s".formatted(jobApplicationDtoJobId));
        }

        return jobRepository.updateJobApplicationFromDto(jobApplication, jobApplicationDto);
    }
}
