package com.mindcare.app.aicoach;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_coach_sessions")
public class AiCoachSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false, unique = true, length = 64)
    private String sessionKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fk_id", nullable = false)
    private StudentEntity student;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_interaction_at", nullable = false)
    private LocalDateTime lastInteractionAt;

    protected AiCoachSessionEntity() {
    }

    public AiCoachSessionEntity(String sessionKey, StudentEntity student) {
        this.sessionKey = sessionKey;
        this.student = student;
        this.status = "ACTIVE";
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        startedAt = now;
        lastInteractionAt = now;
    }

    @PreUpdate
    void onUpdate() {
        lastInteractionAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public StudentEntity getStudent() {
        return student;
    }

    public void touch() {
        lastInteractionAt = LocalDateTime.now();
    }
}
