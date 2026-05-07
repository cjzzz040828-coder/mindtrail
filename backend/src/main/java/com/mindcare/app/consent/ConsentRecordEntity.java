package com.mindcare.app.consent;

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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "consent_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_consent_student_version", columnNames = {"student_fk_id", "version"})
)
public class ConsentRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fk_id", nullable = false)
    private StudentEntity student;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "guardian_consent", nullable = false)
    private boolean guardianConsent;

    @Column(name = "student_assent", nullable = false)
    private boolean studentAssent;

    @Column(name = "camera_training_consent", nullable = false)
    private boolean cameraTrainingConsent;

    @Column(name = "avatar_consent", nullable = false)
    private boolean avatarConsent;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ConsentRecordEntity() {
    }

    public ConsentRecordEntity(StudentEntity student, String version) {
        this.student = student;
        this.version = version;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void replaceConsent(
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent
    ) {
        this.guardianConsent = guardianConsent;
        this.studentAssent = studentAssent;
        this.cameraTrainingConsent = cameraTrainingConsent;
        this.avatarConsent = avatarConsent;
        this.submittedAt = LocalDateTime.now();
    }

    public String getVersion() {
        return version;
    }

    public boolean isGuardianConsent() {
        return guardianConsent;
    }

    public boolean isStudentAssent() {
        return studentAssent;
    }

    public boolean isCameraTrainingConsent() {
        return cameraTrainingConsent;
    }

    public boolean isAvatarConsent() {
        return avatarConsent;
    }
}
