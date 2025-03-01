package com.example.petsitter.users;

import com.example.petsitter.common.exception.*;
import com.example.petsitter.jobs.JobApplicationDto;
import com.example.petsitter.jobs.JobDto;
import com.example.petsitter.jobs.JobService;
import com.example.petsitter.sessions.Permissions;
import com.example.petsitter.sessions.SessionService;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.petsitter.common.CommonConfig.COLLECTIONS_DTO_KEY;
import static com.example.petsitter.common.CommonConfig.MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON;
import static com.example.petsitter.sessions.Permission.Action.*;
import static com.example.petsitter.sessions.Permission.Attribute.*;
import static com.example.petsitter.sessions.Permission.Resource.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {

    private final UserServiceInternal userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> registerUser(@Valid @RequestBody UserDto userDto) {

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userService.registerUser(userDto))
            .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/{uuid}",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE })
    UserDto viewUserWithId(@PathVariable UUID uuid) {

        return userService.viewUserWithId(uuid);
    }

    @PatchMapping(path = "/{uuid}", consumes = MEDIA_TYPE_APPLICATION_MERGE_PATCH_JSON,
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE })
    UserDto modifyUserWithId(@PathVariable UUID uuid, @Valid @RequestBody UserDto userDto) {

        return userService.modifyUserWithId(uuid, userDto);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUserWithId(@PathVariable UUID uuid) {

        userService.deleteUserWithId(uuid);
    }

    @GetMapping(path = "/{uuid}/jobs",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE })
    Map<String, Set<JobDto>> viewJobsForUser(@PathVariable UUID uuid) {

        return Map.of(COLLECTIONS_DTO_KEY, userService.viewJobsForUser(uuid));
    }

    @GetMapping(path = "{uuid}/job-applications",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE })
    Map<String, Set<JobApplicationDto>> viewApplicationsForUser(@PathVariable UUID uuid) {

        return Map.of(COLLECTIONS_DTO_KEY, userService.viewApplicationsForUser(uuid));
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

        var permission = Permissions.getPermission(CREATE, USER, Map.of(USER_DTO_ATT, userDto));

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.CREATE_MSG, optionalReason.orElse("requested User"));
        }

        var invalidArgumentList = new ArrayList<InvalidArgument>();

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

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG, "User %s".formatted(userId)));

        var permission = currentSession.getPermission(VIEW, USER, Map.of(USER_ID_ATT, userId));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "User %s".formatted(userId));
        }

        return userRepository.findDtoWithRolesById(userId)
            .orElseThrow(() -> new NotFoundException("User %s".formatted(userId)));
    }

    @Override
    @Transactional
    public UserDto modifyUserWithId(UUID userId, UserDto userDto) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.MODIFY_MSG,
                "User %s".formatted(userId)));

        var permission = currentSession.getPermission(MODIFY, USER,

            Map.of(
                USER_ID_ATT, userId,
                USER_DTO_ATT, userDto)
        );

        if (permission.isDenied()) {

            var optionalReason = permission.getReason();

            throw new ForbiddenException(ForbiddenException.MODIFY_MSG,
                optionalReason.orElse("User %s".formatted(userId)));
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

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.DELETE_MSG,
                "User %s".formatted(userId)));

        var permission = currentSession.getPermission(DELETE, USER, Map.of(USER_ID_ATT, userId));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.DELETE_MSG, "User %s".formatted(userId));
        }

        if (!currentSession.userId().equals(userId) && !userRepository.existsById(userId)) {
            throw new NotFoundException("User %s".formatted(userId));
        }

        jobService.deleteAllJobsAndApplicationsByOwnerId(userId);

        userRepository.deleteById(userId);
    }

    @Override
    public Set<JobDto> viewJobsForUser(UUID userId) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Jobs for User %s".formatted(userId)));

        var permission = currentSession.getPermission(VIEW, JOB, Map.of(JOB_OWNER_ID_ATT, userId));

        if (permission.isDenied()) {
            throw new ForbiddenException(ForbiddenException.VIEW_MSG, "Jobs for User %s".formatted(userId));
        }

        return jobService.findAllDtoByJobOwnerId(userId);
    }

    @Override
    public Set<JobApplicationDto> viewApplicationsForUser(UUID userId) {

        var currentSession = sessionService.getCurrentSession()
            .orElseThrow(() -> new UnauthorizedException(UnauthorizedException.VIEW_MSG,
                "Job Applications for User %s".formatted(userId)));

        var permission = currentSession.getPermission(VIEW, JOB_APPLICATION,
            Map.of(JOB_APPLICATION_OWNER_ID_ATT, userId));

        if (permission.isDenied()) {
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
