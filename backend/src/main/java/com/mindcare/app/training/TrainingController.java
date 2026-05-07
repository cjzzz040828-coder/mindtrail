package com.mindcare.app.training;

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
@RequestMapping("/api/v1/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/today")
    public ApiResponse<TodayTrainingPayload> today(@RequestParam(defaultValue = "student-demo") String studentId) {
        return ApiResponse.success(toPayload(trainingService.today(studentId)));
    }

    @PostMapping("/tasks/status")
    public ApiResponse<TodayTrainingPayload> updateTaskStatus(@Valid @RequestBody TrainingTaskStatusRequest request) {
        return ApiResponse.success(toPayload(trainingService.updateTaskStatus(
                request.studentId(),
                request.taskId(),
                request.status()
        )));
    }

    private TodayTrainingPayload toPayload(TrainingService.TodayTraining todayTraining) {
        return new TodayTrainingPayload(
                todayTraining.studentId(),
                todayTraining.encouragement(),
                todayTraining.tasks().stream()
                        .map(task -> new TrainingTask(task.id(), task.title(), task.duration(), task.status()))
                        .toList()
        );
    }

    public record TodayTrainingPayload(
            String studentId,
            String encouragement,
            List<TrainingTask> tasks
    ) {
    }

    public record TrainingTask(
            String id,
            String title,
            String duration,
            String status
    ) {
    }

    public record TrainingTaskStatusRequest(
            @NotBlank String studentId,
            @NotBlank String taskId,
            @NotBlank String status
    ) {
    }
}
