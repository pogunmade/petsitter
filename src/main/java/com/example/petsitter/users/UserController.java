package com.example.petsitter.users;

import com.example.petsitter.common.JobApplicationCollectionDto;
import com.example.petsitter.common.JobCollectionDto;
import com.example.petsitter.common.exception.*;
import com.example.petsitter.jobs.JobApplicationDto;
import com.example.petsitter.jobs.JobDto;
import com.example.petsitter.jobs.JobService;
import com.example.petsitter.openapi.ApiProblemResponse;
import com.example.petsitter.sessions.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON;
import static com.example.petsitter.users.User.UserRole.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
class UserController {

    private static final String EXAMPLE_USER_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    private final UserServiceInternal userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register User")
    @SecurityRequirements
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """
            {
               "email": "email@example.com",
               "password": "1Upper1Lower1Number",
               "full_name": "John Smith",
               "roles": ["PET_OWNER"]
            }
            """)})
    )
    @ApiResponse(responseCode = "201" , description = "Created",
        headers = { @Header(name = "Location", description = "User URI", schema = @Schema(type = "string")) }
    )
    @ApiProblemResponse(responseCode = "400", description = "Bad Request")
    ResponseEntity<Void> registerUser(@Valid @RequestBody UserDto userDto) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userService.registerUser(userDto))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE )
    @Operation(summary = "View User")
    @ApiResponse(responseCode = "200", description = "User", content = @Content(
        schema = @Schema(implementation = UserDto.class), examples = {@ExampleObject(value =
            """
            {
              "id": \"""" + EXAMPLE_USER_ID + "\"," + """
              "email": "email@example.com",
              "full_name": "John Smith",
              "roles": ["PET_OWNER"]
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    UserDto viewUserWithId(@Parameter(description = "User ID") @PathVariable UUID uuid) {

        return userService.viewUserWithId(uuid);
    }

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modify User")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
        @ExampleObject(value =
            """
            {
              "email": "new-email@example.com",
              "password": "Y0uKn0wTheDri11",
              "roles": ["PET_OWNER", "PET_SITTER"]
            }
            """)})
    )
    @ApiResponse(responseCode = "200", description = "Modified User", content = @Content(
        schema = @Schema(implementation = UserDto.class), examples = {@ExampleObject(value =
            """
            {
              "id": \"""" + EXAMPLE_USER_ID + "\"," + """
              "email": "new-email@example.com",
              "full_name": "John Smith",
              "roles": ["PET_OWNER", "PET_SITTER"]
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "400", description = "Bad Request")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    UserDto modifyUserWithId(@Parameter(description = "User ID")
                             @PathVariable UUID uuid, @Valid @RequestBody UserDto userDto) {

        return userService.modifyUserWithId(uuid, userDto);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete User")
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    void deleteUserWithId(@Parameter(description = "User ID") @PathVariable UUID uuid) {

        userService.deleteUserWithId(uuid);
    }

    @GetMapping(path = "/{uuid}/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "View Jobs for User")
    @ApiResponse(responseCode = "200", description = "Jobs for User", content = @Content(
        schema = @Schema(implementation = JobCollectionDto.class), examples = {@ExampleObject(value =
            """
            {
              "items": [
                 {
                   "id": "7c7532c1-5ea4-4318-98a5-f9d442fed6cd",
                   "creator_user_id": \"""" + EXAMPLE_USER_ID + "\"," + """
                   "start_time": "2024-12-02 12:00",
                   "end_time": "2024-12-02 14:00",
                   "activity": "Walk",
                   "dog": {
                     "name": "Rambo",
                     "age": "3",
                     "breed": "Bichon Frisé",
                     "size": "6kg"
                   }
                 },
                 {
                   "id": "f50e700a-6dfd-4e23-816b-f1fc9be1e2d3",
                   "creator_user_id": \"""" + EXAMPLE_USER_ID + "\"," + """
                   "start_time": "2024-12-06 12:00",
                   "end_time": "2024-12-06 15:00",
                   "activity": "Walk, House sit",
                   "dog": {
                     "name": "Rambo",
                     "age": "3",
                     "breed": "Bichon Frisé",
                     "size": "6kg"
                   }
                 }
              ]
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "User Not Found")
    JobCollectionDto viewJobsForUser(@Parameter(description = "User ID") @PathVariable UUID uuid) {

        return new JobCollectionDto(userService.viewJobsForUser(uuid));
    }

    @GetMapping(path = "{uuid}/job-applications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "View Job Applications for User")
    @ApiResponse(responseCode = "200", description = "Job Applications for User", content = @Content(
        schema = @Schema(implementation = JobApplicationCollectionDto.class), examples = {@ExampleObject(value =
            """
            {
              "items": [
                {
                  "id": "85b75ddd-4e0a-42b3-9294-b32e524e17cb",
                  "status": "PENDING",
                  "user_id": \"""" + EXAMPLE_USER_ID + "\"," + """
                  "job_id": "d5648770-3663-411e-878f-ce76fc920ecb"
                },
                {
                  "id": "34e30b8e-9bed-4bcd-aafb-3db3037ef255",
                  "status": "ACCEPTED",
                  "user_id": \"""" + EXAMPLE_USER_ID + "\"," + """
                  "job_id": "26dec305-bf14-4b4b-86d6-b3f0fdc9885a"
                }
              ]
            }
            """)})
    )
    @ApiProblemResponse(responseCode = "401", description = "Unauthorized")
    @ApiProblemResponse(responseCode = "403", description = "Forbidden")
    @ApiProblemResponse(responseCode = "404", description = "User Not Found")
    JobApplicationCollectionDto viewApplicationsForUser(@Parameter(description = "User ID")
                                                        @PathVariable UUID uuid) {

        return new JobApplicationCollectionDto(userService.viewApplicationsForUser(uuid));
    }
}

interface UserServiceInternal {

    UUID registerUser(UserDto userDto);

    UserDto viewUserWithId(UUID uuid);

    UserDto modifyUserWithId(UUID uuid, UserDto userDto);

    void deleteUserWithId(UUID uuid);

    Set<JobDto> viewJobsForUser(UUID uuid);

    Set<JobApplicationDto> viewApplicationsForUser(UUID uuid);
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class UserServiceInternalImpl implements UserServiceInternal {

    private final JobService jobService;
    private final SessionService sessionService;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UUID registerUser(UserDto userDto) {

        if (userDto.hasRole(ADMIN)) {

            throw new ForbiddenException(ForbiddenException.INVALID_VALUE_MSG,
                "cannot register User with %s role".formatted(ADMIN));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

        var userDtoId = userDto.getId();

        if (userDtoId != null) {

            invalidArgumentList.add(new InvalidArgument("user", "id",
                "cannot register User with an ID (%s)".formatted(userDtoId)));
        }

        var userDtoEmail = userDto.getEmail();

        if (userDtoEmail == null) {
            invalidArgumentList.add(new InvalidArgument("user", "email", InvalidArgument.NULL_VALUE_MSG));
        }
        else if (userRepository.existsByEmail(userDtoEmail)) {

            invalidArgumentList.add(new InvalidArgument("user", "email",
                "username %s already exists".formatted(userDtoEmail.getAddress())));
        }

        var userDtoPassword = userDto.getPassword();

        if (userDtoPassword == null) {
            invalidArgumentList.add(new InvalidArgument("user", "password", InvalidArgument.NULL_VALUE_MSG));
        }
//        else if (!PasswordValidator.isValid(userDtoPassword)) {
//            invalidArgumentList.add(new InvalidArgument("user", "password", PasswordValidator.PATTERN_DESCRIPTION));
//        }

        var userDtoFullName = userDto.getFullName();

        if (userDtoFullName == null) {
            invalidArgumentList.add(new InvalidArgument("user", "full_name", InvalidArgument.NULL_VALUE_MSG));
        }
        else if (userDtoFullName.isBlank()) {
            invalidArgumentList.add(new InvalidArgument("user", "full_name", InvalidArgument.BLANK_VALUE_MSG));
        }

        var userDtoRoles = userDto.getRoles();

        if (userDtoRoles == null) {
            invalidArgumentList.add(new InvalidArgument("user", "roles", InvalidArgument.NULL_VALUE_MSG));
        }

        if (!invalidArgumentList.isEmpty()) {
            throw new InvalidArgumentException(invalidArgumentList);
        }

        return userRepository.save(userDto).getId();
    }

    @Override
    public UserDto viewUserWithId(UUID userId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "User %s".formatted(userId)));

        if (!currentUserSession.hasRoleOrId(ADMIN, userId)) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "User %s".formatted(userId));
        }

        return userRepository.findDtoWithRolesById(userId)
            .orElseThrow(() -> new NotFoundException("User %s".formatted(userId)));
    }

    @Override
    @Transactional
    public UserDto modifyUserWithId(UUID userId, UserDto userDto) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG,
                "User %s".formatted(userId)));

        if (!currentUserSession.hasRoleOrId(ADMIN, userId)) {
            throw new ForbiddenException(ForbiddenException.MODIFY_MSG, "User %s".formatted(userId));
        }

        var userDtoId = userDto.getId();

        if (userDtoId != null && !userDtoId.equals(userId)) {
            throw new ForbiddenException(ForbiddenException.MODIFY_MSG, "User ID %s".formatted(userId));
        }

        if (userDto.hasRole(ADMIN)) {

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                "User %s with %s role".formatted(userId, ADMIN));
        }

        var userDtoEmail = userDto.getEmail();

        if (userDtoEmail != null && userRepository.existsByEmailAndIdNot(userDtoEmail, userId)) {

            throw new InvalidArgumentException("user", "email",
                "username %s already exists".formatted(userDtoEmail.getAddress()));
        }

        return userRepository.updateUserFromDto(userId, userDto)
            .orElseThrow(() -> new NotFoundException("User %s".formatted(userId)));
    }

    @Override
    @Transactional
    public void deleteUserWithId(UUID userId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.DELETE_MSG,
                "User %s".formatted(userId)));

        var currentUserIsAdmin = currentUserSession.hasRole(ADMIN);

        var currentUserIsUserId = currentUserSession.hasId(userId);

        if ( !(currentUserIsAdmin || currentUserIsUserId) ) {
            throw new ForbiddenException(ForbiddenException.DELETE_MSG, "User %s".formatted(userId));
        }

        if (!currentUserIsUserId && !userRepository.existsById(userId)) {
            throw new NotFoundException("User %s".formatted(userId));
        }

        jobService.deleteAllJobsAndApplicationsByOwnerId(userId);

        userRepository.deleteById(userId);
    }

    @Override
    public Set<JobDto> viewJobsForUser(UUID userId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Jobs for User %s".formatted(userId)));

        if ( !(currentUserSession.hasRole(ADMIN) || currentUserSession.hasRoleAndId(PET_OWNER, userId)) ) {

            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Jobs for User %s".formatted(userId));
        }

        return jobService.findAllDtoByJobOwnerId(userId);
    }

    @Override
    public Set<JobApplicationDto> viewApplicationsForUser(UUID userId) {

        var currentUserSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Job Applications for User %s".formatted(userId)));

        if ( !(currentUserSession.hasRole(ADMIN) || currentUserSession.hasRoleAndId(PET_SITTER, userId)) ) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Job Applications for User %s".formatted(userId));
        }

        return jobService.findAllApplicationsDtoByApplicationOwnerId(userId);
    }
}

class PasswordValidator {

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$";
    static final String PATTERN_DESCRIPTION =
        "must contain at least one digit [0-9], one lowercase character [a-z], one uppercase character [A-Z] " +
            "and be between 8 and 20 characters long";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    static boolean isValid(String password) {
        return pattern.matcher(password).matches();
    }
}
