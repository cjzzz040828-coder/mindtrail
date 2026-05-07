package com.mindcare.app.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_type", nullable = false, length = 32)
    private String actorType;

    @Column(name = "actor_id", nullable = false, length = 128)
    private String actorId;

    @Column(name = "action", nullable = false, length = 128)
    private String action;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false, length = 128)
    private String targetId;

    @Column(name = "metadata_json")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(
            String actorType,
            String actorId,
            String action,
            String targetType,
            String targetId,
            String metadataJson
    ) {
        this.actorType = actorType;
        this.actorId = actorId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadataJson = metadataJson;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getActorType() {
        return actorType;
    }

    public String getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
