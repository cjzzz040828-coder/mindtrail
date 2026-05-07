package com.mindcare.app.aicoach;

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
@Table(name = "ai_coach_events")
public class AiCoachEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AiCoachSessionEntity session;

    @Column(name = "sender", nullable = false, length = 32)
    private String sender;

    @Column(name = "message_summary", nullable = false)
    private String messageSummary;

    @Column(name = "risk_flag", nullable = false)
    private boolean riskFlag;

    @Column(name = "risk_level", nullable = false, length = 16)
    private String riskLevel;

    @Column(name = "safety_action", nullable = false, length = 128)
    private String safetyAction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AiCoachEventEntity() {
    }

    public AiCoachEventEntity(
            AiCoachSessionEntity session,
            String sender,
            String messageSummary,
            boolean riskFlag,
            String riskLevel,
            String safetyAction
    ) {
        this.session = session;
        this.sender = sender;
        this.messageSummary = messageSummary;
        this.riskFlag = riskFlag;
        this.riskLevel = riskLevel;
        this.safetyAction = safetyAction;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
