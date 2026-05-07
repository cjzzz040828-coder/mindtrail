package com.mindcare.app.wellbeing;

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
@Table(name = "training_observation_features")
public class TrainingObservationFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingObservationSessionEntity session;

    @Column(name = "feature_code", nullable = false, length = 64)
    private String featureCode;

    @Column(name = "feature_label", nullable = false, length = 128)
    private String featureLabel;

    @Column(name = "confidence_level", nullable = false, length = 16)
    private String confidenceLevel;

    @Column(name = "observation_summary", nullable = false, length = 255)
    private String observationSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TrainingObservationFeatureEntity() {
    }

    public TrainingObservationFeatureEntity(
            TrainingObservationSessionEntity session,
            String featureCode,
            String featureLabel,
            String confidenceLevel,
            String observationSummary
    ) {
        this.session = session;
        this.featureCode = featureCode;
        this.featureLabel = featureLabel;
        this.confidenceLevel = confidenceLevel;
        this.observationSummary = observationSummary;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public TrainingObservationSessionEntity getSession() {
        return session;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public String getFeatureLabel() {
        return featureLabel;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public String getObservationSummary() {
        return observationSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
