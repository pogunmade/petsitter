package com.example.petsitter.repository;

import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.model.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    @EntityGraph
    Optional<Job> findLazyById(UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"jobApplications"})
    Optional<Job> findWithApplicationsById(UUID id);

    @Query("""
        SELECT j.id AS id,
               j.jobOwner.id AS creatorUserId,
               j.startTime AS startTime,
               j.endTime AS endTime,
               j.activity AS activity,
               j.dog AS dog
          FROM Job j
         WHERE id = :jobId
        """)
    Optional<JobDTO> findDtoById(@Param("jobId") UUID id);

    @Query("""
        SELECT j.id AS id,
               j.jobOwner.id AS creatorUserId,
               j.startTime AS startTime,
               j.endTime AS endTime,
               j.activity AS activity,
               j.dog AS dog
          FROM Job j
        """)
    List<JobDTO> findDtoAll();
}