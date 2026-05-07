package com.mindcare.app.guardian;

import com.mindcare.app.screening.ScreeningSubmissionEntity;
import com.mindcare.app.screening.ScreeningSubmissionRepository;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import com.mindcare.app.training.TrainingService;
import com.mindcare.app.wellbeing.WellbeingObservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuardianSummaryService {

    private static final String PRIVACY_NOTICE = "不展示完整聊天、隐私日记和原始视频，仅提供必要摘要与照护建议。";

    private final StudentService studentService;
    private final ScreeningSubmissionRepository screeningSubmissionRepository;
    private final TrainingService trainingService;
    private final WellbeingObservationService wellbeingObservationService;

    public GuardianSummaryService(
            StudentService studentService,
            ScreeningSubmissionRepository screeningSubmissionRepository,
            TrainingService trainingService,
            WellbeingObservationService wellbeingObservationService
    ) {
        this.studentService = studentService;
        this.screeningSubmissionRepository = screeningSubmissionRepository;
        this.trainingService = trainingService;
        this.wellbeingObservationService = wellbeingObservationService;
    }

    @Transactional
    public GuardianSummary summary(String studentId) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        ScreeningSubmissionEntity latest = screeningSubmissionRepository
                .findFirstByStudent_StudentCodeOrderBySubmittedAtDesc(student.getStudentCode())
                .orElse(null);

        String riskLevel = latest == null ? "LOW" : latest.getRiskLevel();
        int sleepScore = latest == null ? 5 : latest.getSleepScore();
        int stressScore = latest == null ? 4 : latest.getStressScore();
        int trainingCompletionRate = trainingService.todayCompletionRate(student.getStudentCode());
        WellbeingObservationService.ObservationHistoryItem latestObservation = wellbeingObservationService
                .getHistory(student.getStudentCode(), 1)
                .records()
                .stream()
                .findFirst()
                .orElse(null);

        return new GuardianSummary(
                student.getStudentCode(),
                student.getStudentName(),
                student.getSchoolClass().getClassName(),
                "本周",
                toEmotionLabel(riskLevel),
                trainingCompletionRate,
                buildReminders(riskLevel, sleepScore, stressScore).size(),
                buildTrendBars(stressScore),
                buildReminders(riskLevel, sleepScore, stressScore),
                latestObservation == null ? null : new LatestObservationSummary(
                        latestObservation.sceneLabel(),
                        latestObservation.sourceLabel(),
                        latestObservation.indicatorSummary(),
                        latestObservation.observedAt().toString(),
                        latestObservation.featureHighlights().stream()
                                .map(WellbeingObservationService.ObservationFeatureSummary::label)
                                .toList()
                ),
                PRIVACY_NOTICE
        );
    }

    private String toEmotionLabel(String riskLevel) {
        return switch (riskLevel) {
            case "HIGH" -> "需要关注";
            case "MEDIUM" -> "有波动";
            default -> "较稳定";
        };
    }

    private List<Integer> buildTrendBars(int stressScore) {
        int base = Math.max(22, Math.min(72, stressScore * 7));
        return List.of(
                Math.max(18, base - 18),
                Math.max(22, base - 8),
                base,
                Math.min(86, base + 10),
                Math.max(24, base - 4),
                Math.min(82, base + 6),
                Math.max(28, base - 12)
        );
    }

    private List<String> buildReminders(String riskLevel, int sleepScore, int stressScore) {
        if ("HIGH".equals(riskLevel)) {
            return List.of(
                    "建议今天主动陪伴并保持开放沟通",
                    "如孩子持续难受，请联系老师或学校心理老师",
                    "今晚可以一起完成 3 分钟呼吸放松"
            );
        }
        if (sleepScore >= 7 || stressScore >= 6) {
            return List.of(
                    "近几天睡眠或压力有波动",
                    "建议今晚一起完成呼吸练习"
            );
        }
        return List.of(
                "保持规律作息和轻松交流",
                "鼓励孩子完成今日训练"
        );
    }

    public record GuardianSummary(
            String studentId,
            String studentName,
            String className,
            String weekLabel,
            String emotionLabel,
            int trainingCompletionRate,
            int reminderCount,
            List<Integer> trendBars,
            List<String> reminders,
            LatestObservationSummary latestObservation,
            String privacyNotice
    ) {
    }

    public record LatestObservationSummary(
            String sceneLabel,
            String sourceLabel,
            String indicatorSummary,
            String observedAt,
            List<String> focusTags
    ) {
    }
}
