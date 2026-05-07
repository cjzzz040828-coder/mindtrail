package com.mindcare.app.student;

import com.mindcare.app.school.SchoolClassEntity;
import com.mindcare.app.school.SchoolEntity;
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
        name = "students",
        uniqueConstraints = @UniqueConstraint(name = "uk_students_school_student", columnNames = {"school_id", "student_id"})
)
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private SchoolEntity school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClassEntity schoolClass;

    @Column(name = "student_id", nullable = false, length = 64)
    private String studentCode;

    @Column(name = "student_name", nullable = false, length = 128)
    private String studentName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StudentEntity() {
    }

    public StudentEntity(SchoolEntity school, SchoolClassEntity schoolClass, String studentCode, String studentName) {
        this.school = school;
        this.schoolClass = schoolClass;
        this.studentCode = studentCode;
        this.studentName = studentName;
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

    public void updateProfile(SchoolEntity school, SchoolClassEntity schoolClass, String studentName) {
        this.school = school;
        this.schoolClass = schoolClass;
        this.studentName = studentName;
    }

    public Long getId() {
        return id;
    }

    public SchoolEntity getSchool() {
        return school;
    }

    public SchoolClassEntity getSchoolClass() {
        return schoolClass;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getStudentName() {
        return studentName;
    }
}
