package com.mindcare.app.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingTaskRepository extends JpaRepository<TrainingTaskEntity, Long> {

    List<TrainingTaskEntity> findByPlanOrderBySortOrderAsc(TrainingPlanEntity plan);

    Optional<TrainingTaskEntity> findByPlanAndTaskKey(TrainingPlanEntity plan, String taskKey);
}
