package com.mindcare.app.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(
            String actorType,
            String actorId,
            String action,
            String targetType,
            String targetId,
            String metadataJson
    ) {
        auditLogRepository.save(new AuditLogEntity(
                actorType,
                actorId,
                action,
                targetType,
                targetId,
                metadataJson
        ));
    }
}
