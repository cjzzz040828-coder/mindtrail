package com.mindcare.app.wellbeing;

import com.mindcare.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wellbeing")
public class WellbeingObservationController {

    private final WellbeingObservationService wellbeingObservationService;

    public WellbeingObservationController(WellbeingObservationService wellbeingObservationService) {
        this.wellbeingObservationService = wellbeingObservationService;
    }

    @PostMapping("/observations/analyze")
    public ApiResponse<ObservationPayload> analyze(@Valid @RequestBody ObservationRequest request) {
        WellbeingObservationService.ObservationResult result = wellbeingObservationService.analyze(
                request.studentId(),
                request.indicatorCodes(),
                request.sourceType(),
                request.sceneCode(),
                request.captureSummary()
        );
        return ApiResponse.success(new ObservationPayload(
                result.studentId(),
                result.expressionIndicators(),
                result.wellbeingIndicators(),
                result.behaviorSuggestions(),
                result.musicSuggestions(),
                result.dietSuggestions(),
                result.avatarScript(),
                result.disclaimer(),
                result.privacyNotice()
        ));
    }

    @GetMapping("/observations/history")
    public ApiResponse<ObservationHistoryPayload> history(
            @RequestParam String studentId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        WellbeingObservationService.ObservationHistoryResult result = wellbeingObservationService.getHistory(studentId, limit);
        return ApiResponse.success(new ObservationHistoryPayload(
                result.studentId(),
                result.records(),
                result.privacyNotice()
        ));
    }

    public record ObservationRequest(
            @NotBlank String studentId,
            List<String> indicatorCodes,
            String sourceType,
            String sceneCode,
            String captureSummary
    ) {
    }

    public record ObservationPayload(
            String studentId,
            List<WellbeingObservationService.ExpressionIndicator> expressionIndicators,
            List<WellbeingObservationService.WellbeingIndicator> wellbeingIndicators,
            List<WellbeingObservationService.Suggestion> behaviorSuggestions,
            List<WellbeingObservationService.Suggestion> musicSuggestions,
            List<WellbeingObservationService.Suggestion> dietSuggestions,
            String avatarScript,
            String disclaimer,
            String privacyNotice
    ) {
    }

    public record ObservationHistoryPayload(
            String studentId,
            List<WellbeingObservationService.ObservationHistoryItem> records,
            String privacyNotice
    ) {
    }
}
