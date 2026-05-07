package com.mindcare.app.training;

import com.mindcare.app.student.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlanEntity, Long> {

    Optional<TrainingPlanEntity> findByStudentAndPlanDate(StudentEntity student, LocalDate planDate);
}
