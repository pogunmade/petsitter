package com.example.petsitter.common.audit;

import com.example.petsitter.sessions.SessionService;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
public class AuditMetaData {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
class AuditMetaDataConfig {

    private final SessionService sessionService;

    @Bean
    AuditorAware<UUID> auditorProvider() {

        return new AuditorAware<UUID>() {

            @Override
            public Optional<UUID> getCurrentAuditor() {
                return sessionService.getCurrentUserId();
            }
        };
    }
}
