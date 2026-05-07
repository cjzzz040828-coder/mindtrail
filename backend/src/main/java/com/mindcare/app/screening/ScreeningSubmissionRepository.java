package com.mindcare.app.screening;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScreeningSubmissionRepository extends JpaRepository<ScreeningSubmissionEntity, Long> {

    Optional<ScreeningSubmissionEntity> findFirstByStudent_StudentCodeOrderBySubmittedAtDesc(String studentCode);
}
