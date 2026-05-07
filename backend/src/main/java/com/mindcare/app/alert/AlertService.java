package com.mindcare.app.alert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindcare.app.audit.AuditLogEntity;
import com.mindcare.app.audit.AuditLogRepository;
import com.mindcare.app.audit.AuditLogService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.training.TrainingService;
import com.mindcare.app.wellbeing.WellbeingObservationService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AlertService {

    private static final String PRIVACY_NOTICE = "仅展示必要摘要信息，完整隐私内容默认不可见。";

    private final AlertCaseRepository alertCaseRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final TrainingService trainingService;
    private final WellbeingObservationService wellbeingObservationService;
    private final ObjectMapper objectMapper;

    public AlertService(
            AlertCaseRepository alertCaseRepository,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService,
            TrainingService trainingService,
            WellbeingObservationService wellbeingObservationService,
            ObjectMapper objectMapper
    ) {
        this.alertCaseRepository = alertCaseRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.trainingService = trainingService;
        this.wellbeingObservationService = wellbeingObservationService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public TeacherAlerts getTeacherDemo() {
        return getTeacherDemo(null);
    }

    @Transactional(readOnly = true)
    public TeacherAlerts getTeacherDemo(String highlightedAlertId) {
        List<AlertCaseEntity> cases = alertCaseRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AlertItemData> alerts = cases.stream()
                .map(this::toItem)
                .toList();

        AlertDetailData highlightedDetail = cases.stream()
                .filter(alertCase -> highlightedAlertId != null && highlightedAlertId.equals(alertCase.getAlertKey()))
                .findFirst()
                .or(() -> cases.stream()
                        .filter(alertCase -> "HIGH".equals(alertCase.getRiskLevel()))
                        .findFirst())
                .or(() -> cases.stream().findFirst())
                .map(this::toDetail)
                .orElse(null);

        return new TeacherAlerts(alerts, highlightedDetail);
    }

    @Transactional
    public TeacherAlerts updateStatus(String alertId, String status, String actorId, String note) {
        AlertCaseEntity alertCase = alertCaseRepository.findByAlertKey(alertId)
                .orElseThrow(() -> new IllegalArgumentException("预警单不存在：" + alertId));
        alertCase.updateStatus(normalizeStatus(status));
        auditLogService.record(
                "teacher",
                normalizeActor(actorId),
                "UPDATE_ALERT_STATUS",
                "alert_case",
                alertId,
                "{\"status\":\"" + alertCase.getStatus() + "\",\"note\":\"" + sanitize(note) + "\"}"
        );
        return getTeacherDemo(alertId);
    }

    @Transactional(readOnly = true)
    public AlertDetailData getAlertDetail(String alertId) {
        return alertCaseRepository.findByAlertKey(alertId)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("预警单不存在：" + alertId));
    }

    @Transactional
    public void createManualReviewCase(StudentEntity student, String riskLevel, String summary, String reason) {
        String suggestedActions = String.join("|", List.of("联系家长", "联系学校心理老师", "安排线下复核"));
        AlertCaseEntity alertCase = new AlertCaseEntity(
                "alert-" + UUID.randomUUID(),
                student,
                student.getStudentName(),
                student.getSchoolClass().getClassName(),
                riskLevel,
                summary,
                "待处理",
                reason,
                suggestedActions,
                PRIVACY_NOTICE
        );
        alertCaseRepository.save(alertCase);
        auditLogService.record(
                "system",
                "risk-engine",
                "CREATE_ALERT_CASE",
                "alert_case",
                alertCase.getAlertKey(),
                "{\"riskLevel\":\"" + riskLevel + "\"}"
        );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "处理中";
        }
        return status.trim();
    }

    private String normalizeActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "teacher-demo";
        }
        return actorId.trim();
    }

    private String sanitize(String note) {
        if (note == null || note.isBlank()) {
            return "";
        }
        return note.replace("\"", "'").trim();
    }

    private AlertItemData toItem(AlertCaseEntity alertCase) {
        return new AlertItemData(
                alertCase.getAlertKey(),
                alertCase.getStudentName(),
                alertCase.getClassName(),
                alertCase.getRiskLevel(),
                alertCase.getSummary(),
                alertCase.getStatus()
        );
    }

    private AlertDetailData toDetail(AlertCaseEntity alertCase) {
        StudentSnapshot studentSnapshot = buildStudentSnapshot(alertCase.getStudent());
        return new AlertDetailData(
                alertCase.getAlertKey(),
                alertCase.getStudentName(),
                toRiskLabel(alertCase.getRiskLevel()),
                alertCase.getReason(),
                splitActions(alertCase.getSuggestedActions()),
                studentSnapshot == null ? null : studentSnapshot.trainingCompletionRate(),
                studentSnapshot == null ? null : studentSnapshot.latestObservation(),
                studentSnapshot == null ? List.of() : studentSnapshot.recentObservations(),
                studentSnapshot == null ? buildFallbackFollowUpTip() : studentSnapshot.followUpTip(),
                buildActionTimeline(alertCase),
                alertCase.getPrivacyNotice()
        );
    }

    private List<TeacherActionTimeline> buildActionTimeline(AlertCaseEntity alertCase) {
        List<AuditLogEntity> logs = auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                "alert_case",
                alertCase.getAlertKey()
        );

        List<TeacherActionTimeline> timeline = new ArrayList<>();
        boolean hasCreateLog = false;

        for (AuditLogEntity log : logs) {
            TeacherActionTimeline item = toTimelineItem(log, alertCase);
            if (item != null) {
                timeline.add(item);
            }
            if ("CREATE_ALERT_CASE".equals(log.getAction())) {
                hasCreateLog = true;
            }
        }

        if (!hasCreateLog) {
            timeline.add(new TeacherActionTimeline(
                    "created",
                    "系统生成预警摘要",
                    alertCase.getSummary(),
                    "系统自动生成",
                    alertCase.getCreatedAt().toString()
            ));
        }

        return timeline;
    }

    private TeacherActionTimeline toTimelineItem(AuditLogEntity log, AlertCaseEntity alertCase) {
        JsonNode metadata = parseMetadata(log.getMetadataJson());
        String actorLabel = toActorLabel(log.getActorType(), log.getActorId());
        LocalDateTime occurredAt = log.getCreatedAt();

        return switch (log.getAction()) {
            case "UPDATE_ALERT_STATUS" -> new TeacherActionTimeline(
                    "status_update",
                    "老师更新处置状态为" + firstNonBlank(metadata.path("status").asText(), "处理中"),
                    firstNonBlank(metadata.path("note").asText(), "已记录一次老师侧人工跟进。"),
                    actorLabel,
                    occurredAt.toString()
            );
            case "CREATE_ALERT_CASE" -> new TeacherActionTimeline(
                    "created",
                    "系统生成预警摘要",
                    alertCase.getSummary(),
                    actorLabel,
                    occurredAt.toString()
            );
            default -> null;
        };
    }

    private JsonNode parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(metadataJson);
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
    }

    private String toActorLabel(String actorType, String actorId) {
        if ("teacher".equals(actorType)) {
            return "老师 · " + firstNonBlank(actorId, "teacher-demo");
        }
        if ("system".equals(actorType)) {
            return "系统 · " + firstNonBlank(actorId, "risk-engine");
        }
        return firstNonBlank(actorId, "系统");
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary == null || primary.isBlank()) {
            return fallback;
        }
        return primary.trim();
    }

    private StudentSnapshot buildStudentSnapshot(StudentEntity student) {
        if (student == null) {
            return null;
        }

        int trainingCompletionRate = trainingService.todayCompletionRate(student.getStudentCode());
        List<ObservationSummary> recentObservations = wellbeingObservationService.getHistory(student.getStudentCode(), 3)
                .records()
                .stream()
                .map(this::toObservationSummary)
                .toList();
        ObservationSummary latestObservation = recentObservations.stream()
                .findFirst()
                .orElse(null);

        return new StudentSnapshot(
                trainingCompletionRate,
                latestObservation,
                recentObservations,
                buildFollowUpTip(trainingCompletionRate, latestObservation)
        );
    }

    private ObservationSummary toObservationSummary(WellbeingObservationService.ObservationHistoryItem record) {
        return new ObservationSummary(
                record.sceneLabel(),
                record.sourceLabel(),
                record.indicatorSummary(),
                record.observedAt().toString(),
                record.featureHighlights().stream()
                        .map(WellbeingObservationService.ObservationFeatureSummary::label)
                        .toList()
        );
    }

    private TeacherFollowUpTip buildFollowUpTip(int trainingCompletionRate, ObservationSummary latestObservation) {
        if (latestObservation == null) {
            return new TeacherFollowUpTip(
                    "attention",
                    "建议先补一次训练打卡",
                    "当前还没有最近训练观察，建议先提醒学生完成一次状态打卡，再结合量表结果和班级表现做人工判断。"
            );
        }

        if (trainingCompletionRate == 0) {
            return new TeacherFollowUpTip(
                    "urgent",
                    "今日训练尚未启动",
                    "学生今天还没有开始训练，建议老师先做一次低压力提醒，帮助其完成至少 1 个微任务，再观察后续状态。"
            );
        }

        if (trainingCompletionRate < 50) {
            return new TeacherFollowUpTip(
                    "attention",
                    "今日训练完成不足一半",
                    "可围绕最近一次训练场景做温和跟进，优先确认学生现在更需要减压、专注回正还是低负担启动。"
            );
        }

        if (latestObservation.focusTags().contains("紧张迹象")) {
            return new TeacherFollowUpTip(
                    "attention",
                    "最近训练中出现紧张信号",
                    "建议沟通时减少追问，先帮助学生稳定节奏，再决定是否需要进一步复核。"
            );
        }

        return new TeacherFollowUpTip(
                "steady",
                "可继续保持轻量观察",
                "今天已有训练记录，建议结合课堂和班级日常表现继续温和关注，不要仅凭单次状态直接下结论。"
        );
    }

    private TeacherFollowUpTip buildFallbackFollowUpTip() {
        return new TeacherFollowUpTip(
                "attention",
                "当前记录缺少训练档案",
                "这条预警还没有绑定最近训练摘要，建议结合班级观察人工判断，并提醒学生补做一次训练打卡。"
        );
    }

    private String toRiskLabel(String riskLevel) {
        return switch (riskLevel) {
            case "HIGH" -> "高风险";
            case "MEDIUM" -> "中风险";
            default -> "低风险";
        };
    }

    private List<String> splitActions(String suggestedActions) {
        return Arrays.stream(suggestedActions.split("\\|"))
                .filter(action -> !action.isBlank())
                .toList();
    }

    public record TeacherAlerts(
            List<AlertItemData> alerts,
            AlertDetailData highlightedDetail
    ) {
    }

    public record AlertItemData(
            String id,
            String studentName,
            String className,
            String riskLevel,
            String summary,
            String status
    ) {
    }

    public record AlertDetailData(
            String id,
            String studentName,
            String riskLabel,
            String reason,
            List<String> suggestedActions,
            Integer trainingCompletionRate,
            ObservationSummary latestObservation,
            List<ObservationSummary> recentObservations,
            TeacherFollowUpTip followUpTip,
            List<TeacherActionTimeline> actionTimeline,
            String privacyNotice
    ) {
    }

    public record StudentSnapshot(
            int trainingCompletionRate,
            ObservationSummary latestObservation,
            List<ObservationSummary> recentObservations,
            TeacherFollowUpTip followUpTip
    ) {
    }

    public record ObservationSummary(
            String sceneLabel,
            String sourceLabel,
            String indicatorSummary,
            String observedAt,
            List<String> focusTags
    ) {
    }

    public record TeacherFollowUpTip(
            String level,
            String title,
            String detail
    ) {
    }

    public record TeacherActionTimeline(
            String kind,
            String title,
            String detail,
            String actorLabel,
            String occurredAt
    ) {
    }
}
