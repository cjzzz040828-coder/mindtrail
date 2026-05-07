package com.mindcare.app.aicoach;

import com.mindcare.app.alert.AlertService;
import com.mindcare.app.audit.AuditLogService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AiCoachService {

    private static final List<String> CRISIS_WORDS = List.of("自杀", "自伤", "不想活", "伤害自己", "结束生命", "想死", "活不下去", "永别");
    private static final List<String> DISTRESS_WORDS = List.of("崩溃", "绝望", "很痛苦", "撑不住", "焦虑", "害怕", "难受");
    private static final String DISCLAIMER = "AI 陪练只提供训练陪伴与安全提醒，不构成医学诊断。";

    private final AiCoachSessionRepository aiCoachSessionRepository;
    private final AiCoachEventRepository aiCoachEventRepository;
    private final StudentService studentService;
    private final AlertService alertService;
    private final AuditLogService auditLogService;

    public AiCoachService(
            AiCoachSessionRepository aiCoachSessionRepository,
            AiCoachEventRepository aiCoachEventRepository,
            StudentService studentService,
            AlertService alertService,
            AuditLogService auditLogService
    ) {
        this.aiCoachSessionRepository = aiCoachSessionRepository;
        this.aiCoachEventRepository = aiCoachEventRepository;
        this.studentService = studentService;
        this.alertService = alertService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AiCoachReply sendMessage(String studentId, String sessionId, String message) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        AiCoachSessionEntity session = findOrCreateSession(sessionId, student);
        RiskDecision riskDecision = decideRisk(message);

        aiCoachEventRepository.save(new AiCoachEventEntity(
                session,
                "student",
                summarizeStudentMessage(riskDecision),
                riskDecision.crisisDetected(),
                riskDecision.riskLevel(),
                riskDecision.safetyAction()
        ));

        if (riskDecision.crisisDetected()) {
            alertService.createManualReviewCase(
                    student,
                    "HIGH",
                    "AI 陪练中命中危机表达",
                    "系统仅保存危机命中摘要，需由老师或学校心理老师人工复核。"
            );
        }

        String reply = buildReply(riskDecision);
        aiCoachEventRepository.save(new AiCoachEventEntity(
                session,
                "assistant",
                summarizeAssistantReply(riskDecision),
                riskDecision.crisisDetected(),
                riskDecision.riskLevel(),
                riskDecision.safetyAction()
        ));
        session.touch();
        aiCoachSessionRepository.save(session);

        auditLogService.record(
                "student",
                student.getStudentCode(),
                "AI_COACH_MESSAGE",
                "ai_coach_session",
                session.getSessionKey(),
                "{\"riskLevel\":\"" + riskDecision.riskLevel() + "\",\"safetyAction\":\"" + riskDecision.safetyAction() + "\"}"
        );

        return new AiCoachReply(
                session.getSessionKey(),
                reply,
                riskDecision.riskLevel(),
                riskDecision.crisisDetected(),
                DISCLAIMER,
                riskDecision.safetyNotice(),
                riskDecision.suggestedActions()
        );
    }

    private AiCoachSessionEntity findOrCreateSession(String sessionId, StudentEntity student) {
        if (sessionId != null && !sessionId.isBlank()) {
            return aiCoachSessionRepository.findBySessionKey(sessionId.trim())
                    .orElseGet(() -> aiCoachSessionRepository.save(new AiCoachSessionEntity(sessionId.trim(), student)));
        }
        return aiCoachSessionRepository.save(new AiCoachSessionEntity("coach-" + UUID.randomUUID(), student));
    }

    private RiskDecision decideRisk(String message) {
        String content = message == null ? "" : message.trim();
        boolean crisisDetected = CRISIS_WORDS.stream().anyMatch(content::contains);
        if (crisisDetected) {
            return new RiskDecision(
                    "HIGH",
                    true,
                    "CREATE_MANUAL_REVIEW_ALERT",
                    "已触发人工复核提醒。请立即联系身边可信任的大人、家长、老师或学校心理老师；如果有即时危险，请拨打当地紧急电话。",
                    List.of("马上联系可信任成年人", "不要独自待在危险环境中", "如有即时危险请拨打当地紧急电话")
            );
        }

        boolean distressDetected = DISTRESS_WORDS.stream().anyMatch(content::contains);
        if (distressDetected) {
            return new RiskDecision(
                    "MEDIUM",
                    false,
                    "RETURN_SUPPORTIVE_TRAINING",
                    "建议先做短时稳定练习，并在状态持续难受时联系可信任成年人。",
                    List.of("完成 3 分钟呼吸练习", "记录一个当前感受", "必要时联系家长或老师")
            );
        }

        return new RiskDecision(
                "LOW",
                false,
                "RETURN_TRAINING_GUIDE",
                "可以继续进行放松训练；如果状态变化明显，请及时求助。",
                List.of("呼吸放松 3 分钟", "记录今天的一件小事", "保持规律休息")
        );
    }

    private String buildReply(RiskDecision riskDecision) {
        if (riskDecision.crisisDetected()) {
            return "谢谢你告诉我。现在最重要的是保证安全：请马上联系家长、老师或学校心理老师，也尽量不要独处。如果有即时危险，请拨打当地紧急电话。";
        }
        if ("MEDIUM".equals(riskDecision.riskLevel())) {
            return "我听到了，你现在可能有些吃力。我们先做一个很短的稳定练习：慢慢吸气 4 秒，再呼气 4 秒，重复 3 次。";
        }
        return "我在。我们先把注意力放回呼吸：慢慢吸气 4 秒，再呼气 4 秒，重复 3 次，然后给自己一个很小的休息。";
    }

    private String summarizeStudentMessage(RiskDecision riskDecision) {
        if (riskDecision.crisisDetected()) {
            return "学生在 AI 陪练中表达危机相关内容，已进入人工复核流程。";
        }
        if ("MEDIUM".equals(riskDecision.riskLevel())) {
            return "学生在 AI 陪练中表达压力、焦虑或明显难受。";
        }
        return "学生在 AI 陪练中请求日常放松或情绪记录支持。";
    }

    private String summarizeAssistantReply(RiskDecision riskDecision) {
        if (riskDecision.crisisDetected()) {
            return "AI 返回安全提醒、转人工建议和即时求助指引。";
        }
        return "AI 返回短时放松训练和非诊断性陪伴回复。";
    }

    private record RiskDecision(
            String riskLevel,
            boolean crisisDetected,
            String safetyAction,
            String safetyNotice,
            List<String> suggestedActions
    ) {
    }

    public record AiCoachReply(
            String sessionId,
            String reply,
            String riskLevel,
            boolean crisisDetected,
            String disclaimer,
            String safetyNotice,
            List<String> suggestedActions
    ) {
    }
}
