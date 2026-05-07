package com.mindcare.app.aicoach;

import com.mindcare.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-coach")
public class AiCoachController {

    private final AiCoachService aiCoachService;

    public AiCoachController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    @PostMapping("/messages")
    public ApiResponse<AiCoachReplyPayload> sendMessage(@Valid @RequestBody AiCoachMessageRequest request) {
        AiCoachService.AiCoachReply reply = aiCoachService.sendMessage(
                request.studentId(),
                request.sessionId(),
                request.message()
        );
        return ApiResponse.success(new AiCoachReplyPayload(
                reply.sessionId(),
                reply.reply(),
                reply.riskLevel(),
                reply.crisisDetected(),
                reply.disclaimer(),
                reply.safetyNotice(),
                reply.suggestedActions()
        ));
    }

    public record AiCoachMessageRequest(
            @NotBlank String studentId,
            String sessionId,
            @NotBlank String message
    ) {
    }

    public record AiCoachReplyPayload(
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
