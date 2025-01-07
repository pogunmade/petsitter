package com.example.petsitter.jobs;

import com.example.petsitter.common.audit.AuditMetaData;
import com.example.petsitter.users.User;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "Jobs")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_owner_id", nullable = false)
    private User jobOwner;

    @Column(name = "start_time")
    @NotNull
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @NotNull
    private LocalDateTime endTime;

    @Size(max = 500)
    @NotBlank
    private String activity;

    @Embedded
    @Valid
    @NotNull
    private Dog dog;

    @Version
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private short version;

    @Embedded
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AuditMetaData auditMetaData = new AuditMetaData();

    @Embeddable
    @Data
    static class Dog {

        @Size(max=30)
        @NotBlank
        private String name;

        @Min(0)
        @Max(50)
        @NotNull
        private Integer age;

        @Size(max=30)
        @NotBlank
        private String breed;

        @Size(max=30)
        @NotBlank
        private String size;
    }
}
