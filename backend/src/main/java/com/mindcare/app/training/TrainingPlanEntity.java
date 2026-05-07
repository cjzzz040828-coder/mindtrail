package com.mindcare.app.training;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "training_plans",
        uniqueConstraints = @UniqueConstraint(name = "uk_training_plan_student_date", columnNames = {"student_fk_id", "plan_date"})
)
public class TrainingPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_fk_id", nullable = false)
    private StudentEntity student;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "encouragement", nullable = false, length = 255)
    private String encouragement;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TrainingPlanEntity() {
    }

    public TrainingPlanEntity(StudentEntity student, LocalDate planDate, String encouragement) {
        this.student = student;
        this.planDate = planDate;
        this.encouragement = encouragement;
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

    public StudentEntity getStudent() {
        return student;
    }

    public String getEncouragement() {
        return encouragement;
    }
}
