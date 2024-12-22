package com.example.petsitter.jobs;

import com.example.petsitter.users.User;
import com.example.petsitter.users.UserInfrastructureService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class JobRepository {

    private final JpaJobRepository jpaJobRepository;
    private final JpaJobApplicationRepository jpaJobApplicationRepository;

    private final UserInfrastructureService userInfrastructureService;

    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;

    boolean applicationExistsByJobIdAndOwnerId(UUID jobId, UUID ownerId) {

        return jpaJobApplicationRepository.existsByApplicationJobIdAndApplicationOwnerId(jobId, ownerId);
    }

    void deleteAllJobsAndApplicationsByOwnerId(UUID ownerId) {

        jpaJobApplicationRepository.deleteByApplicationOwnerId(ownerId);

        jpaJobApplicationRepository.deleteAllApplicationsForJobsByJobOwnerId(ownerId);

        jpaJobRepository.deleteByJobOwnerId(ownerId);
    }

    void deleteById(UUID jobId) {

        jpaJobApplicationRepository.deleteByApplicationJobId(jobId);

        jpaJobRepository.deleteById(jobId);
    }

    boolean existsById(UUID jobId) {

        return jpaJobRepository.existsById(jobId);
    }

    Set<JobApplicationDto> findAllApplicationsDtoByApplicationOwnerId(UUID applicationOwnerId) {

        return jpaJobApplicationRepository.findAllApplicationsDtoByApplicationOwnerId(applicationOwnerId);
    }

    Set<JobApplicationDto> findAllApplicationsDtoByJobId(UUID jobId) {

        return jpaJobApplicationRepository.findAllApplicationsDtoByJobId(jobId);
    }

    Set<JobDto> findAllDto() {

        return jpaJobRepository.findAllDto();
    }

    Set<JobDto> findAllDtoByJobOwnerId(UUID jobOwnerId) {

        return jpaJobRepository.findAllDtoByJobOwnerId(jobOwnerId);
    }

    Optional<JobApplication> findApplicationWithOwnerAndJobById(UUID applicationId) {

        return jpaJobApplicationRepository.findApplicationWithOwnerAndJobById(applicationId);
    }

    Optional<JobDto> findDtoById(UUID jobId) {

        return jpaJobRepository.findDtoById(jobId);
    }

    Optional<UUID> findJobOwnerIdById(UUID jobId) {

        return jpaJobRepository.findJobOwnerIdById(jobId);
    }

    Optional<Job> findWithJobOwnerById(UUID jobId) {

        return jpaJobRepository.findWithJobOwnerById(jobId);
    }

    JobDto save(UUID jobOwnerId, JobDto jobDto) {

        return jobMapper.toJobDto(jpaJobRepository.save(jobMapper.toJob(jobDto,
            userInfrastructureService.getReferenceById(jobOwnerId))));
    }

    JobApplicationDto saveJobApplication(UUID jobApplicationOwnerId, UUID jobId, JobApplicationDto jobApplicationDto) {

        var jobApplication = jobApplicationMapper.toJobApplication(jobApplicationDto,
            userInfrastructureService.getReferenceById(jobApplicationOwnerId),
            jpaJobRepository.getReferenceById(jobId));

        return jobApplicationMapper.toJobApplicationDto(jpaJobApplicationRepository.save(jobApplication));
    }

    JobApplicationDto updateJobApplicationFromDto(JobApplication jobApplication, JobApplicationDto jobApplicationDto) {

        var userId = jobApplicationDto.getUserId();

        User applicationOwner = userId != null && userId != jobApplication.getApplicationOwner().getId() ?
            userInfrastructureService.getReferenceById(userId) : null;

        var jobId = jobApplicationDto.getJobId();

        Job applicationJob = jobId != null && jobId != jobApplication.getApplicationJob().getId() ?
            jpaJobRepository.getReferenceById(jobId) : null;

        return jobApplicationMapper.toJobApplicationDto(jobApplicationMapper
            .updateJobApplicationFromDto(jobApplication, jobApplicationDto, applicationOwner, applicationJob));
    }

    JobDto updateJobFromDto(Job job, JobDto jobDto) {

        var creatorUserId = jobDto.getCreatorUserId();

        User jobOwner = creatorUserId != null && creatorUserId != job.getJobOwner().getId() ?
            userInfrastructureService.getReferenceById(creatorUserId) : null;

        return jobMapper.toJobDto(jobMapper.updateJobFromDto(job, jobDto, jobOwner));
    }
}

@RepositoryDefinition(domainClass = Job.class, idClass = UUID.class)
interface JpaJobRepository {

    void deleteById(UUID id);

    void deleteByJobOwnerId(UUID ownerId);

    boolean existsById(UUID id);

    @Query("""
        SELECT new com.example.petsitter.jobs.JobDto(
               j.id AS id,
               j.jobOwner.id AS creatorUserId,
               j.startTime AS startTime,
               j.endTime AS endTime,
               j.activity AS activity,
               new com.example.petsitter.jobs.JobDto$DogDto(
                   j.dog.name AS name,
                   j.dog.age AS age,
                   j.dog.breed AS breed,
                   j.dog.size AS size) AS dog)
          FROM Job j
        """)
    Set<JobDto> findAllDto();

    @Query("""
        SELECT new com.example.petsitter.jobs.JobDto(
               j.id AS id,
               j.jobOwner.id AS creatorUserId,
               j.startTime AS startTime,
               j.endTime AS endTime,
               j.activity AS activity,
               new com.example.petsitter.jobs.JobDto$DogDto(
                   j.dog.name AS name,
                   j.dog.age AS age,
                   j.dog.breed AS breed,
                   j.dog.size AS size) AS dog)
          FROM Job j
         WHERE j.jobOwner.id = :ownerId
        """)
    Set<JobDto> findAllDtoByJobOwnerId(@Param("ownerId") UUID id);

    @Query("""
        SELECT new com.example.petsitter.jobs.JobDto(
               j.id AS id,
               j.jobOwner.id AS creatorUserId,
               j.startTime AS startTime,
               j.endTime AS endTime,
               j.activity AS activity,
               new com.example.petsitter.jobs.JobDto$DogDto(
                   j.dog.name AS name,
                   j.dog.age AS age,
                   j.dog.breed AS breed,
                   j.dog.size AS size) AS dog)
          FROM Job j
         WHERE j.id = :jobId
        """)
    Optional<JobDto> findDtoById(@Param("jobId") UUID id);

    @Query("""
        SELECT j.jobOwner.id AS jobOwnerId
          FROM Job j
         WHERE j.id = :id
        """)
    Optional<UUID> findJobOwnerIdById(@Param("id") UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"jobOwner"})
    Optional<Job> findWithJobOwnerById(UUID id);

    Job getReferenceById(UUID id);

    Job save(Job job);
}

@RepositoryDefinition(domainClass = JobApplication.class, idClass = UUID.class)
interface JpaJobApplicationRepository {

    @Modifying
    @Query("""
        DELETE FROM JobApplication ja
         WHERE EXISTS (SELECT 1
                         FROM Job j
                        WHERE j.jobOwner.id = :jobOwnerId
                          AND j.id = ja.applicationJob.id)
        """)
    void deleteAllApplicationsForJobsByJobOwnerId(@Param("jobOwnerId") UUID jobOwnerId);

    void deleteByApplicationJobId(UUID jobId);

    void deleteByApplicationOwnerId(UUID applicationOwnerId);

    boolean existsByApplicationJobIdAndApplicationOwnerId(UUID jobId, UUID ownerId);

    @Query("""
            SELECT new com.example.petsitter.jobs.JobApplicationDto(
                   ja.id AS id,
                   ja.applicationStatus AS status,
                   ja.applicationOwner.id AS userId,
                   ja.applicationJob.id AS jobId)
              FROM JobApplication ja
             WHERE ja.applicationOwner.id = :ownerId
        """)
    Set<JobApplicationDto> findAllApplicationsDtoByApplicationOwnerId(@Param("ownerId") UUID id);

    @Query("""
            SELECT new com.example.petsitter.jobs.JobApplicationDto(
                   ja.id AS id,
                   ja.applicationStatus AS status,
                   ja.applicationOwner.id AS userId,
                   ja.applicationJob.id AS jobId)
              FROM JobApplication ja
             WHERE ja.applicationJob.id = :jobId
            """)
    Set<JobApplicationDto> findAllApplicationsDtoByJobId(@Param("jobId") UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"applicationOwner", "applicationJob"})
    Optional<JobApplication> findApplicationWithOwnerAndJobById(UUID id);

    JobApplication save(JobApplication jobApplication);
}

@Mapper
interface JobMapper {

    @Mapping(target = "id", ignore = true)
    Job toJob(JobDto jobDto, User jobOwner);

    @Mapping(target = "creatorUserId", source = "jobOwner.id")
    JobDto toJobDto(Job job);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Job updateJobFromDto(@MappingTarget Job job, JobDto jobDto, User jobOwner);
}

@Mapper
interface JobApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target="applicationStatus", source="jobApplicationDto.status")
    JobApplication toJobApplication(JobApplicationDto jobApplicationDto, User applicationOwner, Job applicationJob);

    @Mapping(target="status", source="applicationStatus")
    @Mapping(target="userId", source="applicationOwner.id")
    @Mapping(target="jobId", source="applicationJob.id")
    JobApplicationDto toJobApplicationDto(JobApplication jobApplication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target="applicationStatus", source="jobApplicationDto.status")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JobApplication updateJobApplicationFromDto(@MappingTarget JobApplication jobApplication,
                                               JobApplicationDto jobApplicationDto,
                                               User applicationOwner,
                                               Job applicationJob);
}
