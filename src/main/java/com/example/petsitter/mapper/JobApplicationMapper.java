package com.example.petsitter.mapper;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.model.JobApplication;
import org.mapstruct.*;

import java.util.Set;

@Mapper
public interface JobApplicationMapper {

    @Mapping(target="applicationStatus", source="status")
    JobApplication toJobApplication(JobApplicationDTO jobApplicationDTO);

    @Mapping(target="status", source="applicationStatus")
    @Mapping(target="userId", source="applicationOwner.id")
    @Mapping(target="jobId", source="job.id")
    JobApplicationDTO toJobApplicationDTO(JobApplication jobApplication);

    Set<JobApplicationDTO> toDTOSet(Set<JobApplication> jobApplications);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJobApplicationFromDTO(JobApplicationDTO jobApplicationDTO, @MappingTarget JobApplication jobApplication);
}
