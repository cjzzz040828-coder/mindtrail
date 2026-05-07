package com.mindcare.app.wellbeing;

import com.mindcare.app.student.StudentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_observation_sessions")
public class TrainingObservationSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false, unique = true, length = 64)
    private String sessionKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fk_id", nullable = false)
    private StudentEntity student;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "scene_code", nullable = false, length = 64)
    private String sceneCode;

    @Column(name = "indicator_summary", nullable = false, length = 255)
    private String indicatorSummary;

    @Column(name = "raw_video_saved", nullable = false)
    private boolean rawVideoSaved;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TrainingObservationSessionEntity() {
    }

    public TrainingObservationSessionEntity(
            String sessionKey,
            StudentEntity student,
            String sourceType,
            String sceneCode,
            String indicatorSummary
    ) {
        this.sessionKey = sessionKey;
        this.student = student;
        this.sourceType = sourceType;
        this.sceneCode = sceneCode;
        this.indicatorSummary = indicatorSummary;
        this.rawVideoSaved = false;
        this.status = "COMPLETED";
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        startedAt = now;
        finishedAt = now;
        createdAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public String getIndicatorSummary() {
        return indicatorSummary;
    }

    public boolean isRawVideoSaved() {
        return rawVideoSaved;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
