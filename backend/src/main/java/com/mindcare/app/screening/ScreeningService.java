package com.mindcare.app.screening;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindcare.app.alert.AlertService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreeningService {

    private static final String DISCLAIMER = "当前结果仅作为辅助筛查，不构成医学诊断。";
    private static final String SAFETY_NOTICE = "若持续难受或出现危机表达，请及时联系家长、老师或学校心理老师。";

    private final ScreeningSubmissionRepository screeningSubmissionRepository;
    private final StudentService studentService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    public ScreeningService(
            ScreeningSubmissionRepository screeningSubmissionRepository,
            StudentService studentService,
            AlertService alertService,
            ObjectMapper objectMapper
    ) {
        this.screeningSubmissionRepository = screeningSubmissionRepository;
        this.studentService = studentService;
        this.alertService = alertService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ScreeningResult submit(String studentId, int sleepScore, int stressScore, List<String> answers, String note) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        String riskLevel = calculateRiskLevel(stressScore);
        String trend = toTrend(riskLevel);

        screeningSubmissionRepository.save(new ScreeningSubmissionEntity(
                student,
                sleepScore,
                stressScore,
                toJson(answers),
                summarizeNote(note),
                riskLevel,
                trend
        ));

        if ("HIGH".equals(riskLevel)) {
            alertService.createManualReviewCase(
                    student,
                    riskLevel,
                    "自评压力评分达到高风险阈值",
                    "系统仅根据量表分数生成待复核摘要，需由老师或心理老师人工确认。"
            );
        }

        return new ScreeningResult(
                riskLevel,
                trend,
                DISCLAIMER,
                buildTrainingSuggestions(riskLevel),
                SAFETY_NOTICE
        );
    }

    private String calculateRiskLevel(int stressScore) {
        if (stressScore >= 8) {
            return "HIGH";
        }
        if (stressScore >= 5) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String toTrend(String riskLevel) {
        return switch (riskLevel) {
            case "HIGH" -> "高风险趋势";
            case "MEDIUM" -> "中风险趋势";
            default -> "低风险趋势";
        };
    }

    private List<String> buildTrainingSuggestions(String riskLevel) {
        if ("HIGH".equals(riskLevel)) {
            return List.of("呼吸放松 3 分钟", "记录一次情绪日记", "联系可信任成年人");
        }
        return List.of("呼吸放松 3 分钟", "记录一次情绪日记", "今晚尽量提前休息");
    }

    private String toJson(List<String> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private String summarizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String trimmed = note.trim();
        return trimmed.length() <= 200 ? trimmed : trimmed.substring(0, 200);
    }

    public record ScreeningResult(
            String riskLevel,
            String trend,
            String disclaimer,
            List<String> trainingSuggestions,
            String safetyNotice
    ) {
    }
}
