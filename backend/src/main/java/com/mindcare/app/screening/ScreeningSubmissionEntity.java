package com.mindcare.app.screening;

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
@Table(name = "screening_submissions")
public class ScreeningSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fk_id", nullable = false)
    private StudentEntity student;

    @Column(name = "sleep_score", nullable = false)
    private int sleepScore;

    @Column(name = "stress_score", nullable = false)
    private int stressScore;

    @Column(name = "answers_json", nullable = false)
    private String answersJson;

    @Column(name = "note_summary")
    private String noteSummary;

    @Column(name = "risk_level", nullable = false, length = 16)
    private String riskLevel;

    @Column(name = "trend", nullable = false, length = 32)
    private String trend;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    protected ScreeningSubmissionEntity() {
    }

    public ScreeningSubmissionEntity(
            StudentEntity student,
            int sleepScore,
            int stressScore,
            String answersJson,
            String noteSummary,
            String riskLevel,
            String trend
    ) {
        this.student = student;
        this.sleepScore = sleepScore;
        this.stressScore = stressScore;
        this.answersJson = answersJson;
        this.noteSummary = noteSummary;
        this.riskLevel = riskLevel;
        this.trend = trend;
    }

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    public int getSleepScore() {
        return sleepScore;
    }

    public int getStressScore() {
        return stressScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getTrend() {
        return trend;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
