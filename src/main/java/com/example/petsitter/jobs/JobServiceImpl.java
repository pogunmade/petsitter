package com.example.petsitter.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Override
    @Transactional
    public void deleteAllJobsAndApplicationsByOwnerId(UUID id) {

        jobRepository.deleteAllJobsAndApplicationsByOwnerId(id);
    }

    @Override
    public Set<JobApplicationDto> findAllApplicationsDtoByApplicationOwnerId(UUID id) {

        return jobRepository.findAllApplicationsDtoByApplicationOwnerId(id);
    }

    @Override
    public Set<JobDto> findAllDtoByJobOwnerId(UUID id) {

        return jobRepository.findAllDtoByJobOwnerId(id);
    }
}
