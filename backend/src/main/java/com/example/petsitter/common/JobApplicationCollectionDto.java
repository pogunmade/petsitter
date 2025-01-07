package com.example.petsitter.common;

import com.example.petsitter.jobs.JobApplicationDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

@Schema(name = "JobApplicationCollection")
public class JobApplicationCollectionDto extends CollectionDto<JobApplicationDto> {

    public JobApplicationCollectionDto(Collection<JobApplicationDto> items) {
        super(items);
    }
}
