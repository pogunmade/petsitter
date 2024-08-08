package com.example.petsitter.model;

import com.example.petsitter.model.audit.AuditMetaData;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.type.YesNoConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SoftDelete(columnName = "deleted", converter = YesNoConverter.class)
@Table(name = "Users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @OneToMany(mappedBy = "jobOwner", cascade = CascadeType.ALL)
    private Set<Job> jobs;

    @OneToMany(mappedBy = "applicationOwner", cascade = CascadeType.ALL)
    private Set<JobApplication> jobApplications;

    @Version
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long version;

    @Embedded
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AuditMetaData auditMetaData;
}