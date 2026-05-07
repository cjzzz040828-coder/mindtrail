package com.mindcare.app.alert;

import com.mindcare.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/teacher/demo")
    public ApiResponse<TeacherAlertPayload> demo(@RequestParam(required = false) String highlightAlertId) {
        return ApiResponse.success(toPayload(alertService.getTeacherDemo(highlightAlertId)));
    }

    @PostMapping("/{alertId}/status")
    public ApiResponse<TeacherAlertPayload> updateStatus(
            @PathVariable String alertId,
            @Valid @RequestBody AlertStatusUpdateRequest request
    ) {
        return ApiResponse.success(toPayload(alertService.updateStatus(
                alertId,
                request.status(),
                request.actorId(),
                request.note()
        )));
    }

    private TeacherAlertPayload toPayload(AlertService.TeacherAlerts teacherAlerts) {
        return new TeacherAlertPayload(
                teacherAlerts.alerts().stream()
                        .map(alert -> new AlertItem(
                                alert.id(),
                                alert.studentName(),
                                alert.className(),
                                alert.riskLevel(),
                                alert.summary(),
                                alert.status()
                        ))
                        .toList(),
                toDetail(teacherAlerts.highlightedDetail())
        );
    }

    private AlertDetail toDetail(AlertService.AlertDetailData detail) {
        if (detail == null) {
            return null;
        }
        return new AlertDetail(
                detail.id(),
                detail.studentName(),
                detail.riskLabel(),
                detail.reason(),
                detail.suggestedActions(),
                detail.trainingCompletionRate(),
                detail.latestObservation(),
                detail.recentObservations(),
                detail.followUpTip(),
                detail.actionTimeline(),
                detail.privacyNotice()
        );
    }

    public record TeacherAlertPayload(
            List<AlertItem> alerts,
            AlertDetail highlightedDetail
    ) {
    }

    public record AlertItem(
            String id,
            String studentName,
            String className,
            String riskLevel,
            String summary,
            String status
    ) {
    }

    public record AlertDetail(
            String id,
            String studentName,
            String riskLabel,
            String reason,
            List<String> suggestedActions,
            Integer trainingCompletionRate,
            AlertService.ObservationSummary latestObservation,
            List<AlertService.ObservationSummary> recentObservations,
            AlertService.TeacherFollowUpTip followUpTip,
            List<AlertService.TeacherActionTimeline> actionTimeline,
            String privacyNotice
    ) {
    }

    public record AlertStatusUpdateRequest(
            @NotBlank String status,
            String actorId,
            String note
    ) {
    }
}
