package com.mindcare.app.aicoach;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiCoachSessionRepository extends JpaRepository<AiCoachSessionEntity, Long> {

    Optional<AiCoachSessionEntity> findBySessionKey(String sessionKey);
}
