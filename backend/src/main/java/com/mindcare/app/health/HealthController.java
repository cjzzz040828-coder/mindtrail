package com.mindcare.app.health;

import com.mindcare.app.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<HealthPayload> health() {
        return ApiResponse.success(new HealthPayload(
                "UP",
                "mental-health-backend",
                OffsetDateTime.now().toString()
        ));
    }

    public record HealthPayload(
            String status,
            String service,
            String timestamp
    ) {
    }
}
