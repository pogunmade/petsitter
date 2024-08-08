package com.example.petsitter.controller;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    path = "/jobs",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobDTO jobDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(jobService.createJob(jobDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public List<JobDTO> viewAllJobs() {

        return jobService.viewAllJobs();
    }

    @GetMapping("/{uuid}")
    public JobDTO viewJobWithId(@PathVariable UUID id) {

        return jobService.viewJobsWithId(id);
    }

    @PutMapping("/{uuid}")
    public JobDTO modifyJobWithId(@PathVariable UUID id, @Valid @RequestBody JobDTO jobDTO) {

        return jobService.modifyJobWithId(id, jobDTO);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJobWithId(@PathVariable UUID id) {

        jobService.deleteJobWithId(id);
    }

    @GetMapping("/{uuid}/job-applications")
    public Set<JobApplicationDTO> viewApplicationsForJob(@PathVariable UUID id) {

        return jobService.viewApplicationsForJob(id);
    }

    @PostMapping("/{uuid}/job-applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createJobApplication(@PathVariable UUID id,
                                                  @Valid @RequestBody JobApplicationDTO jobApplicationDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/job-applications/{id}")
            .buildAndExpand(jobService.createJobApplication(id, jobApplicationDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }
}
