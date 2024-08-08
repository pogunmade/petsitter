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

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@SoftDelete(columnName = "deleted", converter = YesNoConverter.class)
@Table(name = "JobApplications")
@Data
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    private JobApplicationStatus applicationStatus;

    @ManyToOne(cascade = CascadeType.ALL)
    private User applicationOwner;

    @ManyToOne(cascade = CascadeType.ALL)
    private Job job;

    @Version
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long version;

    @Embedded
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AuditMetaData auditMetaData;
}