package com.mindcare.app.wellbeing;

import com.mindcare.app.audit.AuditLogService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class WellbeingObservationService {

    private static final String DISCLAIMER = "状态观察仅用于训练辅助，不构成医学诊断，也不参与风险分级。表情、眼神和注意力信号可能受疲劳、光线、镜头角度等因素影响。";
    private static final String PRIVACY_NOTICE = "默认不保存原始图片、视频或完整私密内容，仅记录观察摘要和建议结果。";

    private final StudentService studentService;
    private final AuditLogService auditLogService;
    private final TrainingObservationSessionRepository trainingObservationSessionRepository;
    private final TrainingObservationFeatureRepository trainingObservationFeatureRepository;

    public WellbeingObservationService(
            StudentService studentService,
            AuditLogService auditLogService,
            TrainingObservationSessionRepository trainingObservationSessionRepository,
            TrainingObservationFeatureRepository trainingObservationFeatureRepository
    ) {
        this.studentService = studentService;
        this.auditLogService = auditLogService;
        this.trainingObservationSessionRepository = trainingObservationSessionRepository;
        this.trainingObservationFeatureRepository = trainingObservationFeatureRepository;
    }

    @Transactional
    public ObservationResult analyze(
            String studentId,
            List<String> indicatorCodes,
            String sourceType,
            String sceneCode,
            String captureSummary
    ) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        Set<String> normalizedCodes = normalizeCodes(indicatorCodes);
        String normalizedSourceType = normalizeSourceType(sourceType);
        String normalizedSceneCode = normalizeSceneCode(sceneCode, normalizedCodes);
        String normalizedCaptureSummary = normalizeCaptureSummary(captureSummary);

        List<ExpressionIndicator> expressionIndicators = normalizedCodes.stream()
                .map(this::toExpressionIndicator)
                .toList();
        List<WellbeingIndicator> wellbeingIndicators = buildWellbeingIndicators(normalizedCodes);
        List<Suggestion> behaviorSuggestions = buildBehaviorSuggestions(normalizedCodes);
        List<Suggestion> musicSuggestions = buildMusicSuggestions(normalizedCodes);
        List<Suggestion> dietSuggestions = buildDietSuggestions(normalizedCodes);
        String avatarScript = buildAvatarScript(normalizedCodes);
        TrainingObservationSessionEntity session = saveObservationSession(
                student,
                normalizedCodes,
                expressionIndicators,
                normalizedSourceType,
                normalizedSceneCode,
                normalizedCaptureSummary
        );

        auditLogService.record(
                "student",
                student.getStudentCode(),
                "WELLBEING_OBSERVATION",
                "training_observation_session",
                session.getSessionKey(),
                "{\"indicatorCount\":" + normalizedCodes.size()
                        + ",\"rawVideoSaved\":false"
                        + ",\"sourceType\":\"" + normalizedSourceType + "\""
                        + ",\"sceneCode\":\"" + normalizedSceneCode + "\"}"
        );

        return new ObservationResult(
                student.getStudentCode(),
                expressionIndicators,
                wellbeingIndicators,
                behaviorSuggestions,
                musicSuggestions,
                dietSuggestions,
                avatarScript,
                DISCLAIMER,
                PRIVACY_NOTICE
        );
    }

    @Transactional(readOnly = true)
    public ObservationHistoryResult getHistory(String studentId, int limit) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        int normalizedLimit = Math.min(Math.max(limit, 1), 8);

        List<TrainingObservationSessionEntity> sessions = trainingObservationSessionRepository
                .findByStudent_StudentCodeOrderByCreatedAtDesc(student.getStudentCode(), PageRequest.of(0, normalizedLimit));

        if (sessions.isEmpty()) {
            return new ObservationHistoryResult(student.getStudentCode(), List.of(), PRIVACY_NOTICE);
        }

        List<Long> sessionIds = sessions.stream()
                .map(TrainingObservationSessionEntity::getId)
                .toList();
        Map<Long, List<ObservationFeatureSummary>> featureMap = new LinkedHashMap<>();

        trainingObservationFeatureRepository.findBySession_IdInOrderByCreatedAtAsc(sessionIds)
                .forEach(feature -> featureMap.computeIfAbsent(feature.getSession().getId(), unused -> new java.util.ArrayList<>())
                        .add(new ObservationFeatureSummary(
                                feature.getFeatureCode(),
                                feature.getFeatureLabel(),
                                feature.getConfidenceLevel(),
                                feature.getObservationSummary()
                        )));

        List<ObservationHistoryItem> records = sessions.stream()
                .map(session -> new ObservationHistoryItem(
                        session.getSessionKey(),
                        session.getSourceType(),
                        resolveSourceLabel(session.getSourceType()),
                        session.getSceneCode(),
                        resolveSceneLabel(session.getSceneCode()),
                        session.getIndicatorSummary(),
                        session.isRawVideoSaved(),
                        session.getStatus(),
                        session.getCreatedAt(),
                        featureMap.getOrDefault(session.getId(), List.of())
                ))
                .toList();

        return new ObservationHistoryResult(student.getStudentCode(), records, PRIVACY_NOTICE);
    }

    private TrainingObservationSessionEntity saveObservationSession(
            StudentEntity student,
            Set<String> normalizedCodes,
            List<ExpressionIndicator> expressionIndicators,
            String sourceType,
            String sceneCode,
            String captureSummary
    ) {
        TrainingObservationSessionEntity session = trainingObservationSessionRepository.save(
                new TrainingObservationSessionEntity(
                        "obs-" + UUID.randomUUID(),
                        student,
                        sourceType,
                        sceneCode,
                        buildIndicatorSummary(normalizedCodes, captureSummary)
                )
        );

        trainingObservationFeatureRepository.saveAll(expressionIndicators.stream()
                .map(indicator -> new TrainingObservationFeatureEntity(
                        session,
                        indicator.code(),
                        indicator.label(),
                        indicator.confidence(),
                        indicator.observation()
                ))
                .toList());

        return session;
    }

    private String buildIndicatorSummary(Set<String> normalizedCodes, String captureSummary) {
        String codeSummary = String.join("|", normalizedCodes);
        if (captureSummary == null || captureSummary.isBlank()) {
            return truncate(codeSummary, 255);
        }
        return truncate(captureSummary + " | 指标:" + codeSummary, 255);
    }

    private String normalizeSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return "manual_indicator_input";
        }
        String normalized = sourceType.trim()
                .toLowerCase()
                .replace('-', '_')
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_");
        if (normalized.isBlank()) {
            return "manual_indicator_input";
        }
        return truncate(normalized, 32);
    }

    private String normalizeSceneCode(String sceneCode, Set<String> codes) {
        if (sceneCode == null || sceneCode.isBlank()) {
            return resolveSceneCode(codes);
        }
        String normalized = sceneCode.trim()
                .toUpperCase()
                .replace('-', '_')
                .replaceAll("[^A-Z0-9_]+", "_")
                .replaceAll("_+", "_");
        if (normalized.isBlank()) {
            return resolveSceneCode(codes);
        }
        return truncate(normalized, 64);
    }

    private String normalizeCaptureSummary(String captureSummary) {
        if (captureSummary == null || captureSummary.isBlank()) {
            return "";
        }
        return truncate(captureSummary.trim().replaceAll("\\s+", " "), 220);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String resolveSourceLabel(String sourceType) {
        return switch (sourceType) {
            case "camera_preview" -> "摄像头预览辅助采集";
            case "manual_indicator_input" -> "训练场景默认指标";
            default -> "训练辅助输入";
        };
    }

    private String resolveSceneLabel(String sceneCode) {
        return switch (sceneCode) {
            case "TENSION_RELIEF" -> "舒压呼吸";
            case "FOCUS_RECOVERY" -> "专注回正";
            case "ENERGY_RESTORE" -> "能量唤醒";
            default -> "通用训练";
        };
    }

    private String resolveSceneCode(Set<String> codes) {
        if (codes.contains("TENSION")) {
            return "TENSION_RELIEF";
        }
        if (codes.contains("GAZE_DRIFT") || codes.contains("LOW_ATTENTION")) {
            return "FOCUS_RECOVERY";
        }
        if (codes.contains("LOW_ENERGY")) {
            return "ENERGY_RESTORE";
        }
        return "GENERAL_TRAINING";
    }

    private Set<String> normalizeCodes(List<String> indicatorCodes) {
        Set<String> codes = new LinkedHashSet<>();
        if (indicatorCodes != null) {
            indicatorCodes.stream()
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase())
                    .forEach(codes::add);
        }
        if (codes.isEmpty()) {
            codes.add("LOW_ATTENTION");
            codes.add("LOW_ENERGY");
        }
        return codes;
    }

    private ExpressionIndicator toExpressionIndicator(String code) {
        return switch (code) {
            case "GAZE_DRIFT" -> new ExpressionIndicator(code, "视线游离", "训练中视线离开屏幕较多", "medium");
            case "LOW_ATTENTION" -> new ExpressionIndicator(code, "注意力波动", "连续专注时间偏短", "medium");
            case "LOW_ENERGY" -> new ExpressionIndicator(code, "面部活力偏低", "表情变化较少、互动回应偏慢", "medium");
            case "TENSION" -> new ExpressionIndicator(code, "紧张迹象", "眉眼或嘴角呈现紧绷趋势", "medium");
            default -> new ExpressionIndicator(code, "一般观察信号", "训练中出现需要关注的状态变化", "low");
        };
    }

    private List<WellbeingIndicator> buildWellbeingIndicators(Set<String> codes) {
        if (codes.contains("GAZE_DRIFT") && codes.contains("LOW_ATTENTION")) {
            return List.of(
                    new WellbeingIndicator("ATTENTION_FATIGUE", "可能存在注意力疲劳", "建议先降低任务强度，做短时稳定练习。"),
                    new WellbeingIndicator("LOW_ENGAGEMENT", "互动投入度偏低", "可以用更轻松的数字人引导和较短训练时长。")
            );
        }
        if (codes.contains("LOW_ENERGY")) {
            return List.of(new WellbeingIndicator("LOW_ENERGY_STATE", "心理能量可能偏低", "建议从低负担行为开始，而不是强迫完成高强度任务。"));
        }
        if (codes.contains("TENSION")) {
            return List.of(new WellbeingIndicator("TENSION_STATE", "可能处于紧张或压力状态", "建议优先做呼吸放松、肌肉放松或安全感练习。"));
        }
        return List.of(new WellbeingIndicator("STABLE_TRAINING_STATE", "训练状态相对平稳", "可以继续当前节奏，并保持温和提醒。"));
    }

    private List<Suggestion> buildBehaviorSuggestions(Set<String> codes) {
        if (codes.contains("GAZE_DRIFT") || codes.contains("LOW_ATTENTION")) {
            return List.of(
                    new Suggestion("BREATHING", "3 分钟方块呼吸", "吸气 4 秒、停 2 秒、呼气 4 秒，重复 5 轮。"),
                    new Suggestion("GROUNDING", "5-4-3-2-1 感官定位", "说出看到的 5 个物体、听到的 4 种声音，把注意力拉回当下。")
            );
        }
        if (codes.contains("LOW_ENERGY")) {
            return List.of(
                    new Suggestion("SUNLIGHT", "到窗边或户外站 3 分钟", "用低门槛行动帮助身体重新启动。"),
                    new Suggestion("MICRO_TASK", "完成一个 30 秒小任务", "整理桌面一角或喝一口水，建立可完成感。")
            );
        }
        return List.of(new Suggestion("MINDFUL", "正念专注 5 分钟", "跟随数字人完成一次温和专注练习。"));
    }

    private List<Suggestion> buildMusicSuggestions(Set<String> codes) {
        if (codes.contains("TENSION")) {
            return List.of(new Suggestion("SOFT_PIANO", "低速钢琴/自然白噪音", "建议 60-80 BPM，音量保持较低。"));
        }
        return List.of(new Suggestion("LIGHT_AMBIENT", "轻环境音乐", "选择无歌词、节奏稳定的背景音乐，避免过度刺激。"));
    }

    private List<Suggestion> buildDietSuggestions(Set<String> codes) {
        if (codes.contains("LOW_ENERGY")) {
            return List.of(new Suggestion("HYDRATION_SNACK", "补水和轻食", "可选择温水、牛奶、香蕉或坚果；避免用高糖饮料快速提神。"));
        }
        return List.of(new Suggestion("REGULAR_MEAL", "规律饮食提醒", "保持正常三餐和饮水，不把情绪状态简单归因于饮食。"));
    }

    private String buildAvatarScript(Set<String> codes) {
        if (codes.contains("GAZE_DRIFT") || codes.contains("LOW_ATTENTION")) {
            return "我注意到你可能有点难集中。没关系，我们不急着完成很多内容，先一起做 3 轮呼吸，把注意力慢慢带回来。";
        }
        if (codes.contains("LOW_ENERGY")) {
            return "你现在可能能量不太高。我们先做一个很小的动作：坐稳、喝口水，然后给自己 30 秒安静时间。";
        }
        return "你做得不错。我们保持这个节奏，继续完成一个短短的放松练习。";
    }

    public record ObservationResult(
            String studentId,
            List<ExpressionIndicator> expressionIndicators,
            List<WellbeingIndicator> wellbeingIndicators,
            List<Suggestion> behaviorSuggestions,
            List<Suggestion> musicSuggestions,
            List<Suggestion> dietSuggestions,
            String avatarScript,
            String disclaimer,
            String privacyNotice
    ) {
    }

    public record ExpressionIndicator(
            String code,
            String label,
            String observation,
            String confidence
    ) {
    }

    public record WellbeingIndicator(
            String code,
            String label,
            String explanation
    ) {
    }

    public record Suggestion(
            String code,
            String title,
            String detail
    ) {
    }

    public record ObservationHistoryResult(
            String studentId,
            List<ObservationHistoryItem> records,
            String privacyNotice
    ) {
    }

    public record ObservationHistoryItem(
            String sessionKey,
            String sourceType,
            String sourceLabel,
            String sceneCode,
            String sceneLabel,
            String indicatorSummary,
            boolean rawVideoSaved,
            String status,
            LocalDateTime observedAt,
            List<ObservationFeatureSummary> featureHighlights
    ) {
    }

    public record ObservationFeatureSummary(
            String code,
            String label,
            String confidence,
            String observation
    ) {
    }
}
