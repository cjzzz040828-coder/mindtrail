package com.mindcare.app.screening;

import com.mindcare.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping("/template")
    public ApiResponse<ScreeningTemplatePayload> template() {
        return ApiResponse.success(new ScreeningTemplatePayload(
                "基础情绪与压力自评",
                3,
                "约 2 分钟",
                List.of(
                        new ScreeningQuestion("q1", "最近一周，你的睡眠情况如何？", List.of("很好", "一般", "不太好", "很差")),
                        new ScreeningQuestion("q2", "最近一周，你是否经常感到压力明显？", List.of("几乎没有", "偶尔", "经常", "几乎每天")),
                        new ScreeningQuestion("q3", "最近一周，你是否愿意和他人交流自己的感受？", List.of("愿意", "有时愿意", "不太愿意", "几乎不愿意"))
                )
        ));
    }

    @PostMapping("/submit")
    public ApiResponse<ScreeningResultPayload> submit(@Valid @RequestBody ScreeningSubmitRequest request) {
        ScreeningService.ScreeningResult result = screeningService.submit(
                request.studentId(),
                request.sleepScore(),
                request.stressScore(),
                request.answers(),
                request.note()
        );
        return ApiResponse.success(new ScreeningResultPayload(
                result.riskLevel(),
                result.trend(),
                result.disclaimer(),
                result.trainingSuggestions(),
                result.safetyNotice()
        ));
    }

    public record ScreeningTemplatePayload(
            String title,
            int questionCount,
            String estimatedDuration,
            List<ScreeningQuestion> questions
    ) {
    }

    public record ScreeningQuestion(
            String id,
            String title,
            List<String> options
    ) {
    }

    public record ScreeningSubmitRequest(
            @NotBlank String studentId,
            @Min(0) @Max(10) int sleepScore,
            @Min(0) @Max(10) int stressScore,
            @NotEmpty List<String> answers,
            String note
    ) {
    }

    public record ScreeningResultPayload(
            String riskLevel,
            String trend,
            String disclaimer,
            List<String> trainingSuggestions,
            String safetyNotice
    ) {
    }
}
