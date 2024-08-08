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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SoftDelete(columnName = "deleted", converter = YesNoConverter.class)
@Table(name = "Jobs")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String activity;

    @Embedded
    private Dog dog;

    @ManyToOne(cascade = CascadeType.ALL)
    private User jobOwner;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
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