package com.example.petsitter.mapper;

import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.model.Job;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(uses = {DogMapper.class})
public interface JobMapper {

    Job toJob(JobDTO jobDTO);

    @Mapping(target="creatorUserId", source="jobOwner.id")
    JobDTO toJobDTO(Job job);

    List<JobDTO> toDTOList(List<Job> job);

    Set<JobDTO> toDTOSet(Set<Job> job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJobFromDTO(JobDTO jobDto, @MappingTarget Job job);
}
