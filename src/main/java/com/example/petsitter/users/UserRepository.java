package com.example.petsitter.users;

import com.example.petsitter.common.Email;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class UserRepository {

    private final JpaUserRepository jpaUserRepository;

    private final UserMapper userMapper;

    void deleteById(UUID id) {

        jpaUserRepository.deleteById(id);
    }

    boolean existsByEmail(Email email) {

        return jpaUserRepository.existsByEmail(email.getAddress());
    }

    boolean existsByEmailAndIdNot(Email email, UUID id) {

        return jpaUserRepository.existsByEmailAndIdNot(email.getAddress(), id);
    }

    boolean existsById(UUID id) {

        return jpaUserRepository.existsById(id);
    }

    boolean existsByIdAndRole(UUID userId, User.UserRole role) {

        return jpaUserRepository.existsByIdAndRolesIn(userId, Set.of(role));
    }

    Optional<UserDto> findDtoWithPasswordAndRolesByEmailAddress(String emailAddress) {

        return jpaUserRepository.findWithRolesByEmail(emailAddress)
            .map(userMapper::toUserDtoWithPassword);
    }

    Optional<UserDto> findDtoWithRolesById(UUID id) {

        return jpaUserRepository.findWithRolesById(id)
            .map(userMapper::toUserDto);
    }

    User getReferenceById(UUID id) {

        return jpaUserRepository.getReferenceById(id);
    }

    UserDto save(UserDto userDto) {

        return userMapper.toUserDto(jpaUserRepository.save(userMapper.toUserEncodePassword(userDto)));
    }

    Optional<UserDto> updateUserFromDto(UUID id, UserDto userDto) {

        return jpaUserRepository.findWithRolesById(id)
            .map(user -> userMapper.updateUserFromDtoEncodePassword(user, userDto))
            .map(userMapper::toUserDto);
    }
}

@RepositoryDefinition(domainClass = User.class, idClass = UUID.class)
interface JpaUserRepository {

    void deleteById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsById(UUID id);

    boolean existsByIdAndRolesIn(UUID userId, Set<User.UserRole> role);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles"})
    Optional<User> findWithRolesByEmail(String email);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"roles"})
    Optional<User> findWithRolesById(UUID id);

    User getReferenceById(UUID id);

    User save(User user);
}

@Mapper(uses = UserMapper.PasswordEncoderMapper.class)
interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email.address")
    @Mapping(target = "password", qualifiedBy = EncodedMapping.class)
    User toUserEncodePassword(UserDto userDTO);

    @Mapping(target = "password", ignore = true)
    UserDto toUserDto(User user);

    UserDto toUserDtoWithPassword(User user);

    @Mapping(target = "address", source = "email")
    Email toEmail(String email);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email.address")
    @Mapping(target = "password", qualifiedBy = EncodedMapping.class)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User updateUserFromDtoEncodePassword(@MappingTarget User user, UserDto userDto);

    @Component
    @RequiredArgsConstructor
    class PasswordEncoderMapper {

        private final PasswordEncoder passwordEncoder;

        @EncodedMapping
        public String encode(String password) {
            return passwordEncoder.encode(password);
        }
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface EncodedMapping {}
}
