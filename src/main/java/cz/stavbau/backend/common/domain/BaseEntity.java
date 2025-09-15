package cz.stavbau.backend.common.domain;

import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
import lombok.*;
import jakarta.persistence.Id;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.*; import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@MappedSuperclass @EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue @UuidGenerator private UUID id;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
    @CreatedBy private UUID createdBy;
    @LastModifiedBy private UUID updatedBy;
}
