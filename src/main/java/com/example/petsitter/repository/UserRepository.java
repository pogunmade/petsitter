package com.example.petsitter.repository;

import com.example.petsitter.dto.UserDTO;
import com.example.petsitter.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph
    Optional<User> findLazyById(UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles"})
    Optional<User> findWithRolesById(UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"jobs"})
    Optional<User> findWithJobsById(UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"jobApplications"})
    Optional<User> findWithApplicationsById(UUID id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles"})
    Optional<UserDTO> findDtoById(UUID id);
}
