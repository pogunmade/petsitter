package com.example.petsitter.controller;

import com.example.petsitter.dto.JobApplicationDTO;
import com.example.petsitter.dto.JobDTO;
import com.example.petsitter.dto.UserDTO;
import com.example.petsitter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(
    path = "/users",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userService.registerUser(userDTO))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{uuid}")
    public UserDTO viewUserWithId(@PathVariable UUID uuid) {

        return userService.viewUserWithId(uuid);
    }

    @PutMapping("/{uuid}")
    public UserDTO modifyUserWithId(@PathVariable UUID uuid, @Valid @RequestBody UserDTO userDTO) {

        return userService.modifyUserWithId(uuid, userDTO);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserWithId(@PathVariable UUID uuid) {

        userService.deleteUserWithId(uuid);
    }

    @GetMapping(value = "/{uuid}/jobs")
    public Set<JobDTO> viewJobsForUser(@PathVariable UUID uuid) {

        return userService.viewJobsForUser(uuid);
    }

    @GetMapping(value = "{uuid}/job-applications")
    public Set<JobApplicationDTO> viewApplicationsForUser(@PathVariable UUID uuid) {

        return userService.viewApplicationsForUser(uuid);
    }
}
