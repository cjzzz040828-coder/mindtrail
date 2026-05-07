package com.mindcare.app.wellbeing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TrainingObservationSessionRepository extends JpaRepository<TrainingObservationSessionEntity, Long> {

    Optional<TrainingObservationSessionEntity> findTopByOrderByIdDesc();

    List<TrainingObservationSessionEntity> findByStudent_StudentCodeOrderByCreatedAtDesc(String studentCode, Pageable pageable);
}
