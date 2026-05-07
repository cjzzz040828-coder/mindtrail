package com.mindcare.app.consent;

import com.mindcare.app.student.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecordEntity, Long> {

    Optional<ConsentRecordEntity> findFirstByStudent_StudentCodeAndVersionOrderBySubmittedAtDesc(String studentCode, String version);

    Optional<ConsentRecordEntity> findByStudentAndVersion(StudentEntity student, String version);
}
