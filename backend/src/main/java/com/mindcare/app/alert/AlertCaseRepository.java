package com.mindcare.app.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertCaseRepository extends JpaRepository<AlertCaseEntity, Long> {

    Optional<AlertCaseEntity> findByAlertKey(String alertKey);
}
