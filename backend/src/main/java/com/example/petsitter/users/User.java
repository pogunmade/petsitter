package com.example.petsitter.users;

import com.example.petsitter.common.audit.AuditMetaData;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Email
    @NotNull
    private String email;

    @NotBlank
    private String password;

    @Column(name = "full_name")
    @NotBlank
    @Size(max=50)
    private String fullName;

    @ElementCollection
    @CollectionTable(name = "User_Roles")
    @Enumerated(EnumType.STRING)
    @NotNull
    @Size(min=1)
    private Set<UserRole> roles;

    @Version
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private short version;

    @Embedded
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AuditMetaData auditMetaData = new AuditMetaData();

    public enum UserRole { PET_OWNER, PET_SITTER, ADMIN }
}
