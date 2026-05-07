package com.mindcare.app.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);
}
