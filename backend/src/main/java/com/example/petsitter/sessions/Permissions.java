package com.example.petsitter.sessions;

import com.example.petsitter.jobs.JobApplication;
import com.example.petsitter.jobs.JobApplicationDto;
import com.example.petsitter.jobs.JobDto;
import com.example.petsitter.users.UserDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.petsitter.jobs.JobApplication.JobApplicationStatus.*;
import static com.example.petsitter.sessions.Permission.Action.CREATE;
import static com.example.petsitter.sessions.Permission.Action.VIEW;
import static com.example.petsitter.sessions.Permission.Attribute.*;
import static com.example.petsitter.sessions.Permission.Decision.DENIED;
import static com.example.petsitter.sessions.Permission.Resource.JOB;
import static com.example.petsitter.sessions.Permission.Resource.USER;
import static com.example.petsitter.users.User.UserRole.*;

public class Permissions {

    public static Permission getPermission(Permission.Action action, Permission.Resource resource,
                                           Map<Permission.Attribute, Object> attributes) {

        if (resource == USER && action == CREATE) {

            if (!(attributes.get(USER_DTO_ATT) instanceof UserDto userDto)) {
                return Permission.IS_DENIED;
            }

            var userDtoId = userDto.getId();

            if (userDtoId != null) {
                return new Permission(DENIED, "User with ID %s".formatted(userDtoId));
            }

            var userDtoRoles = userDto.getRoles();

            if (userDtoRoles != null && userDtoRoles.contains(ADMIN)) {
                return new Permission(DENIED, "User with %s role".formatted(ADMIN));
            }

            return Permission.IS_GRANTED;
        }

        return Permission.IS_DENIED;
    }

    static Permission getPermission(Permission.Action action, Permission.Resource resource, Session session) {

        if (resource == JOB && action == VIEW) {

            if (Stream.of(PET_SITTER, ADMIN).anyMatch(session.roles()::contains)) {
                return Permission.IS_GRANTED;
            }
        }

        return Permission.IS_DENIED;
    }

    static Permission getPermission(Permission.Action action, Permission.Resource resource,
                                    Map<Permission.Attribute, Object> attributes, Session session) {

        return switch (resource) {


            case USER -> switch (action) {


                case CREATE -> Permission.IS_DENIED;


                case VIEW, DELETE -> {

                    if (!(attributes.get(USER_ID_ATT) instanceof UUID userId)) {
                        yield Permission.IS_DENIED;
                    }

                    if (session.userId().equals(userId) || session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case MODIFY -> {

                    if (!(attributes.get(USER_ID_ATT) instanceof UUID userId &&
                          attributes.get(USER_DTO_ATT) instanceof UserDto userDto)) {

                        yield Permission.IS_DENIED;
                    }

                    var userDtoId = userDto.getId();

                    if (userDtoId != null && !userDtoId.equals(userId)) {
                        yield new Permission(DENIED, "User ID %s".formatted(userId));
                    }

                    if (session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    var userDtoRoles = userDto.getRoles();

                    if (userDtoRoles != null && userDtoRoles.contains(ADMIN)) {
                        yield new Permission(DENIED, "User %s with %s role".formatted(userId, ADMIN));
                    }

                    if (session.userId().equals(userId)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }
            };


            case JOB -> switch (action) {


                case CREATE -> {

                    if (!(attributes.get(JOB_OWNER_ID_ATT) instanceof UUID jobOwnerId &&
                          attributes.get(JOB_DTO_ATT) instanceof JobDto jobDto)) {

                        yield Permission.IS_DENIED;
                    }

                    var jobDtoId = jobDto.getId();

                    if (jobDtoId != null) {
                        yield new Permission(DENIED, "Job with ID %s".formatted(jobDtoId));
                    }

                    if (session.userId().equals(jobOwnerId) && session.roles().contains(PET_OWNER)) {
                        yield Permission.IS_GRANTED;
                    }

                    if (session.roles().contains(ADMIN)) {

                        if (jobDto.getCreatorUserId() == null) {

                            yield new Permission(DENIED,
                                "creating Job as administrator, creator user ID (Pet Owner) must be specified");
                        }

                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case VIEW -> {

                    if (Stream.of(PET_SITTER, ADMIN).anyMatch(session.roles()::contains)) {
                        yield Permission.IS_GRANTED;
                    }

                    if (!(attributes.get(JOB_OWNER_ID_ATT) instanceof UUID jobOwnerId)) {
                        yield Permission.IS_DENIED;
                    }

                    if (session.userId().equals(jobOwnerId) && session.roles().contains(PET_OWNER)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case MODIFY -> {

                    if (!(attributes.get(JOB_ID_ATT) instanceof UUID jobId &&
                          attributes.get(JOB_OWNER_ID_ATT) instanceof UUID jobOwnerId &&
                          attributes.get(JOB_DTO_ATT) instanceof JobDto jobDto)) {

                        yield Permission.IS_DENIED;
                    }

                    var jobDtoId = jobDto.getId();

                    if (jobDtoId != null && !jobDtoId.equals(jobId)) {
                        yield new Permission(DENIED, "Job ID %s".formatted(jobId));
                    }

                    if (session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    var jobDtoCreatorUserId = jobDto.getCreatorUserId();

                    if (jobDtoCreatorUserId != null && !jobDtoCreatorUserId.equals(jobOwnerId)) {
                        yield new Permission(DENIED, "Job creator user ID, Job %s".formatted(jobId));
                    }

                    if (session.userId().equals(jobOwnerId) && session.roles().contains(PET_OWNER)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case DELETE -> {

                    if (!(attributes.get(JOB_OWNER_ID_ATT) instanceof UUID jobOwnerId)) {
                        yield Permission.IS_DENIED;
                    }

                    if (session.userId().equals(jobOwnerId) && session.roles().contains(PET_OWNER)) {
                        yield Permission.IS_GRANTED;
                    }

                    if (session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }
            };


            case JOB_APPLICATION -> switch (action) {


                case CREATE -> {

                    if (!(attributes.get(JOB_APPLICATION_OWNER_ID_ATT) instanceof UUID jobApplicationOwnerId &&
                          attributes.get(JOB_APPLICATION_DTO_ATT) instanceof JobApplicationDto jobApplicationDto)) {

                        yield Permission.IS_DENIED;
                    }

                    var jobApplicationDtoId = jobApplicationDto.getId();

                    if (jobApplicationDtoId != null) {
                        yield new Permission(DENIED, "Job Application with ID %s".formatted(jobApplicationDtoId));
                    }

                    var jobApplicationDtoStatus = jobApplicationDto.getStatus();

                    if (jobApplicationDtoStatus == null) {
                        yield new Permission(DENIED, "Job Application status must be specified");
                    }

                    var sessionUserIsAdmin = session.roles().contains(ADMIN);

                    if (session.userId().equals(jobApplicationOwnerId) && session.roles().contains(PET_SITTER)) {

                        if (jobApplicationDtoStatus == PENDING) {
                            yield Permission.IS_GRANTED;
                        }

                        if (!sessionUserIsAdmin) {
                            yield new Permission(DENIED, "Job Application status must equal %s".formatted(PENDING));
                        }
                    }

                    if (sessionUserIsAdmin) {

                        if (jobApplicationDto.getUserId() == null) {

                            yield new Permission(DENIED,
                                "creating Job Application as administrator, user ID (Pet Sitter) must be specified");
                        }

                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case VIEW -> {

                    var sessionUserId = session.userId();

                    if (sessionUserId.equals(attributes.get(JOB_APPLICATION_OWNER_ID_ATT)) &&
                        session.roles().contains(PET_SITTER)) {

                        yield Permission.IS_GRANTED;
                    }

                    if (sessionUserId.equals(attributes.get(JOB_OWNER_ID_ATT)) && session.roles().contains(PET_OWNER)) {
                        yield Permission.IS_GRANTED;
                    }

                    if (session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    yield Permission.IS_DENIED;
                }


                case MODIFY -> {

                    if (!(attributes.get(JOB_APPLICATION_ATT) instanceof JobApplication jobApplication &&
                          attributes.get(JOB_APPLICATION_DTO_ATT) instanceof JobApplicationDto jobApplicationDto)) {

                        yield Permission.IS_DENIED;
                    }

                    var jobApplicationId = jobApplication.getId();
                    var jobApplicationDtoId = jobApplicationDto.getId();

                    if (jobApplicationDtoId != null && !jobApplicationDtoId.equals(jobApplicationId)) {
                        yield new Permission(DENIED, "Job Application ID %s".formatted(jobApplicationId));
                    }

                    if (session.roles().contains(ADMIN)) {
                        yield Permission.IS_GRANTED;
                    }

                    var jobApplicationOwnerId = jobApplication.getApplicationOwner().getId();
                    var jobApplicationDtoUserId = jobApplicationDto.getUserId();

                    if (jobApplicationDtoUserId != null && !jobApplicationDtoUserId.equals(jobApplicationOwnerId)) {

                        yield new Permission(DENIED,
                            "Job Application user ID. Job Application %s".formatted(jobApplicationId));
                    }

                    var jobApplicationDtoJobId = jobApplicationDto.getJobId();

                    if (jobApplicationDtoJobId != null &&
                        !jobApplicationDtoJobId.equals(jobApplication.getApplicationJob().getId())) {

                        yield new Permission(DENIED,
                            "Job Application Job ID. Job Application %s".formatted(jobApplicationId));
                    }

                    var asPetSitter =
                        session.userId().equals(jobApplicationOwnerId) && session.roles().contains(PET_SITTER);

                    var jobApplicationJobOwnerId = jobApplication.getApplicationJob().getJobOwner().getId();
                    var asPetOwner =
                        session.userId().equals(jobApplicationJobOwnerId) && session.roles().contains(PET_OWNER);

                    var validPetSitterStatusList = List.of(PENDING, WITHDRAWN);
                    var validPetOwnerStatusList = List.of(ACCEPTED, PENDING, REJECTED);
                    var jobApplicationDtoStatus = jobApplicationDto.getStatus();

                    if (asPetSitter) {

                        if (validPetSitterStatusList.contains(jobApplicationDtoStatus)) {
                            yield Permission.IS_GRANTED;
                        }

                        if (!asPetOwner) {

                            yield new Permission(DENIED, "modifying Job Application as Pet Sitter, status must be in %s"
                                .formatted(validPetSitterStatusList));
                        }
                    }

                    if (asPetOwner) {

                        if (validPetOwnerStatusList.contains(jobApplicationDtoStatus)) {
                            yield Permission.IS_GRANTED;
                        }

                        yield new Permission(DENIED, "modifying Job Application as Pet Owner, status must be in %s"
                            .formatted(validPetOwnerStatusList));
                    }

                    yield Permission.IS_DENIED;
                }


                case DELETE -> Permission.IS_DENIED;
            };
        };
    }
}
