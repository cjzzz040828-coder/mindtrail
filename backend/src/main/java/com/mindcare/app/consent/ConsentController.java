package com.mindcare.app.consent;

import com.mindcare.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping("/status")
    public ApiResponse<ConsentStatusPayload> status(@RequestParam String studentId) {
        return ApiResponse.success(toPayload(consentService.getStatus(studentId)));
    }

    @PostMapping("/submit")
    public ApiResponse<ConsentStatusPayload> submit(@Valid @RequestBody ConsentSubmitRequest request) {
        return ApiResponse.success(toPayload(consentService.submit(
                request.studentId(),
                request.guardianConsent(),
                request.studentAssent(),
                request.cameraTrainingConsent(),
                request.avatarConsent()
        )));
    }

    private ConsentStatusPayload toPayload(
            ConsentService.ConsentStatus status
    ) {
        return new ConsentStatusPayload(
                status.studentId(),
                status.version(),
                status.guardianConsent(),
                status.studentAssent(),
                status.cameraTrainingConsent(),
                status.avatarConsent(),
                status.allRequiredCompleted(),
                status.nextStep()
        );
    }

    public record ConsentSubmitRequest(
            @NotBlank String studentId,
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent
    ) {
    }

    public record ConsentStatusPayload(
            String studentId,
            String version,
            boolean guardianConsent,
            boolean studentAssent,
            boolean cameraTrainingConsent,
            boolean avatarConsent,
            boolean allRequiredCompleted,
            String nextStep
    ) {
    }
}
