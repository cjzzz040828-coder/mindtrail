package com.mindcare.app.bootstrap;

import com.mindcare.app.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bootstrap")
public class BootstrapController {

    @GetMapping
    public ApiResponse<BootstrapPayload> bootstrap() {
        return ApiResponse.success(new BootstrapPayload(
                "心理健康 App MVP",
                "student",
                List.of("student", "guardian", "teacher", "school_counselor"),
                List.of("screening", "training", "ai_coach", "guardian_summary", "teacher_alert")
        ));
    }

    public record BootstrapPayload(
            String appName,
            String defaultRole,
            List<String> roles,
            List<String> featureFlags
    ) {
    }
}
