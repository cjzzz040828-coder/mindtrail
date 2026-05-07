package com.mindcare.app.guardian;

import com.mindcare.app.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guardian")
public class GuardianController {

    private final GuardianSummaryService guardianSummaryService;

    public GuardianController(GuardianSummaryService guardianSummaryService) {
        this.guardianSummaryService = guardianSummaryService;
    }

    @GetMapping("/summary")
    public ApiResponse<GuardianSummaryPayload> summary(@RequestParam(defaultValue = "S2024001") String studentId) {
        GuardianSummaryService.GuardianSummary summary = guardianSummaryService.summary(studentId);
        return ApiResponse.success(new GuardianSummaryPayload(
                summary.studentId(),
                summary.studentName(),
                summary.className(),
                summary.weekLabel(),
                summary.emotionLabel(),
                summary.trainingCompletionRate(),
                summary.reminderCount(),
                summary.trendBars(),
                summary.reminders(),
                summary.latestObservation(),
                summary.privacyNotice()
        ));
    }

    public record GuardianSummaryPayload(
            String studentId,
            String studentName,
            String className,
            String weekLabel,
            String emotionLabel,
            int trainingCompletionRate,
            int reminderCount,
            java.util.List<Integer> trendBars,
            java.util.List<String> reminders,
            GuardianSummaryService.LatestObservationSummary latestObservation,
            String privacyNotice
    ) {
    }
}
