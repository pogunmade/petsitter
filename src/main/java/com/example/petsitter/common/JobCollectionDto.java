package com.example.petsitter.common;

import com.example.petsitter.jobs.JobDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

@Schema(name = "JobCollection")
public class JobCollectionDto extends CollectionDto<JobDto> {

    public JobCollectionDto(Collection<JobDto> items) {
        super(items);
    }
}
