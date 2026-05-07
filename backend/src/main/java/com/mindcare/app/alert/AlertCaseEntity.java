package com.mindcare.app.alert;

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
@Table(name = "alert_cases")
public class AlertCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_key", nullable = false, unique = true, length = 64)
    private String alertKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fk_id")
    private StudentEntity student;

    @Column(name = "student_name", nullable = false, length = 128)
    private String studentName;

    @Column(name = "class_name", nullable = false, length = 128)
    private String className;

    @Column(name = "risk_level", nullable = false, length = 16)
    private String riskLevel;

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "suggested_actions", nullable = false)
    private String suggestedActions;

    @Column(name = "privacy_notice", nullable = false, length = 255)
    private String privacyNotice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AlertCaseEntity() {
    }

    public AlertCaseEntity(
            String alertKey,
            StudentEntity student,
            String studentName,
            String className,
            String riskLevel,
            String summary,
            String status,
            String reason,
            String suggestedActions,
            String privacyNotice
    ) {
        this.alertKey = alertKey;
        this.student = student;
        this.studentName = studentName;
        this.className = className;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.status = status;
        this.reason = reason;
        this.suggestedActions = suggestedActions;
        this.privacyNotice = privacyNotice;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getAlertKey() {
        return alertKey;
    }

    public StudentEntity getStudent() {
        return student;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getSuggestedActions() {
        return suggestedActions;
    }

    public String getPrivacyNotice() {
        return privacyNotice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}
