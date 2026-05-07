package com.mindcare.app.consent;

import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

    private static final String CURRENT_VERSION = "v1.0";

    private final ConsentRecordRepository consentRecordRepository;
    private final StudentService studentService;

    public ConsentService(ConsentRecordRepository consentRecordRepository, StudentService studentService) {
        this.consentRecordRepository = consentRecordRepository;
        this.studentService = studentService;
    }

    @Transactional(readOnly = true)
    public ConsentStatus getStatus(String studentId) {
        return consentRecordRepository.findFirstByStudent_StudentCodeAndVersionOrderBySubmittedAtDesc(studentId, CURRENT_VERSION)
                .map(record -> buildStatus(
                        studentId,
                        record.getVersion(),
                        record.isGuardianConsent(),
                        record.isStudentAssent(),
                        record.isCameraTrainingConsent(),
                        record.isAvatarConsent()
                ))
                .orElseGet(() -> buildStatus(studentId, CURRENT_VERSION, false, false, false, false));
    }

    @Transactional
    public ConsentStatus submit(
            String studentId,
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent
    ) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        ConsentRecordEntity record = consentRecordRepository.findByStudentAndVersion(student, CURRENT_VERSION)
                .orElseGet(() -> new ConsentRecordEntity(student, CURRENT_VERSION));
        record.replaceConsent(guardianConsent, studentAssent, cameraTrainingConsent, avatarConsent);
        consentRecordRepository.save(record);
        return buildStatus(student.getStudentCode(), CURRENT_VERSION, guardianConsent, studentAssent, cameraTrainingConsent, avatarConsent);
    }

    private ConsentStatus buildStatus(
            String studentId,
            String version,
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent
    ) {
        boolean allRequiredCompleted = guardianConsent && studentAssent && cameraTrainingConsent;
        return new ConsentStatus(
                studentId,
                version,
                guardianConsent,
                studentAssent,
                cameraTrainingConsent,
                avatarConsent,
                allRequiredCompleted,
                allRequiredCompleted ? "SCREENING" : "CONSENT"
        );
    }

    public record ConsentStatus(
            String studentId,
            String version,
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent,
            boolean allRequiredCompleted,
            String nextStep
    ) {
    }
}
