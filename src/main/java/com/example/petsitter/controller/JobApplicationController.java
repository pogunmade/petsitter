package com.example.petsitter.controller;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    path = "/job-applications",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PutMapping("/{uuid}")
    public JobApplicationDTO modifyJobApplicationWithId(@PathVariable UUID id,
                                                        @Valid @RequestBody JobApplicationDTO jobApplicationDTO) {

        return jobApplicationService.modifyJobApplicationWithId(id, jobApplicationDTO);
    }
}