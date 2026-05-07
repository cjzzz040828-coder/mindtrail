package com.mindcare.app.wellbeing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TrainingObservationFeatureRepository extends JpaRepository<TrainingObservationFeatureEntity, Long> {

    List<TrainingObservationFeatureEntity> findBySession_IdInOrderByCreatedAtAsc(Collection<Long> sessionIds);
}
