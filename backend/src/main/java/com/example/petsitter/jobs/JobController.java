package com.example.petsitter.jobs;

import com.example.petsitter.common.exception.*;
import com.example.petsitter.sessions.SessionService;
import com.example.petsitter.users.UserService;
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

import static com.example.petsitter.common.CommonConfig.*;
import static com.example.petsitter.sessions.Permission.Action.*;
import static com.example.petsitter.sessions.Permission.Attribute.*;
import static com.example.petsitter.sessions.Permission.Resource.JOB;
import static com.example.petsitter.sessions.Permission.Resource.JOB_APPLICATION;
import static com.example.petsitter.users.User.UserRole.PET_OWNER;
import static com.example.petsitter.users.User.UserRole.PET_SITTER;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
class JobController {

    private final JobServiceInternal jobService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createJob(@Valid @RequestBody JobDto jobDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(jobService.createJob(jobDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Set<JobDto>> viewAllJobs() {

        return Map.of(COLLECTIONS_DTO_KEY, jobService.viewAllJobs());
    }

    @GetMapping(path = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    JobDto viewJobWithId(@PathVariable UUID uuid) {

        return jobService.viewJobWithId(uuid);
    }

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = MediaType.APPLICATION_JSON_VALUE)
    JobDto modifyJobWithId(@PathVariable UUID uuid, @Valid @RequestBody JobDto jobDTO) {

        return jobService.modifyJobWithId(uuid, jobDTO);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteJobWithId(@PathVariable UUID uuid) {

        jobService.deleteJobWithId(uuid);
    }

    @GetMapping(path = "/{uuid}/job-applications", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Set<JobApplicationDto>> viewApplicationsForJob(@PathVariable UUID uuid) {

         return Map.of(COLLECTIONS_DTO_KEY, jobService.viewApplicationsForJob(uuid));
    }

    @PostMapping(path = "/{uuid}/job-applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createJobApplication(@PathVariable UUID uuid,
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
class JobApplicationController {

    private final JobServiceInternal jobService;

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = MediaType.APPLICATION_JSON_VALUE)
    JobApplicationDto modifyJobApplicationWithId(@PathVariable UUID uuid,
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
