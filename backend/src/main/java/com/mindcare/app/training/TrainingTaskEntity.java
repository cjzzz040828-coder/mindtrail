package com.mindcare.app.training;

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
        name = "training_tasks",
        uniqueConstraints = @UniqueConstraint(name = "uk_training_task_plan_key", columnNames = {"plan_id", "task_key"})
)
public class TrainingTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlanEntity plan;

    @Column(name = "task_key", nullable = false, length = 64)
    private String taskKey;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "duration_label", nullable = false, length = 64)
    private String durationLabel;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TrainingTaskEntity() {
    }

    public TrainingTaskEntity(
            TrainingPlanEntity plan,
            String taskKey,
            String title,
            String durationLabel,
            String status,
            int sortOrder
    ) {
        this.plan = plan;
        this.taskKey = taskKey;
        this.title = title;
        this.durationLabel = durationLabel;
        this.status = status;
        this.sortOrder = sortOrder;
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

    public String getTaskKey() {
        return taskKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDurationLabel() {
        return durationLabel;
    }

    public String getStatus() {
        return status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}
