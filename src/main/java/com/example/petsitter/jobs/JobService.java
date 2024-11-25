package com.example.petsitter.jobs;

import java.util.Set;
import java.util.UUID;

public interface JobService {

    void deleteAllJobsAndApplicationsByOwnerId(UUID id);

    Set<JobApplicationDto> findAllApplicationsDtoByApplicationOwnerId(UUID id);

    Set<JobDto> findAllDtoByJobOwnerId(UUID id);
}
