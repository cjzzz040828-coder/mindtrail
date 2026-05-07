package com.mindcare.app.training;

import com.mindcare.app.audit.AuditLogService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final TrainingTaskRepository trainingTaskRepository;
    private final StudentService studentService;
    private final AuditLogService auditLogService;

    public TrainingService(
            TrainingPlanRepository trainingPlanRepository,
            TrainingTaskRepository trainingTaskRepository,
            StudentService studentService,
            AuditLogService auditLogService
    ) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.trainingTaskRepository = trainingTaskRepository;
        this.studentService = studentService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TodayTraining today(String studentId) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        TrainingPlanEntity plan = getTodayPlan(student);
        return toTodayTraining(student, plan);
    }

    @Transactional
    public TodayTraining updateTaskStatus(String studentId, String taskId, String status) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        TrainingPlanEntity plan = getTodayPlan(student);
        TrainingTaskEntity task = trainingTaskRepository.findByPlanAndTaskKey(plan, taskId)
                .orElseThrow(() -> new IllegalArgumentException("训练任务不存在：" + taskId));
        task.updateStatus(normalizeStatus(status));
        trainingTaskRepository.save(task);
        auditLogService.record(
                "student",
                student.getStudentCode(),
                "UPDATE_TRAINING_TASK",
                "training_task",
                taskId,
                "{\"status\":\"" + task.getStatus() + "\"}"
        );
        return toTodayTraining(student, plan);
    }

    @Transactional
    public int todayCompletionRate(String studentId) {
        StudentEntity student = studentService.findOrCreatePlaceholder(studentId);
        TrainingPlanEntity plan = getTodayPlan(student);
        List<TrainingTaskEntity> tasks = trainingTaskRepository.findByPlanOrderBySortOrderAsc(plan);
        if (tasks.isEmpty()) {
            return 0;
        }
        long completed = tasks.stream()
                .filter(task -> "completed".equals(task.getStatus()))
                .count();
        return (int) Math.round(completed * 100.0 / tasks.size());
    }

    private TrainingPlanEntity getTodayPlan(StudentEntity student) {
        LocalDate today = LocalDate.now();
        return trainingPlanRepository.findByStudentAndPlanDate(student, today)
                .orElseGet(() -> createDefaultPlan(student, today));
    }

    private TodayTraining toTodayTraining(StudentEntity student, TrainingPlanEntity plan) {
        List<TrainingTaskData> tasks = trainingTaskRepository.findByPlanOrderBySortOrderAsc(plan).stream()
                .map(task -> new TrainingTaskData(
                        task.getTaskKey(),
                        task.getTitle(),
                        task.getDurationLabel(),
                        task.getStatus()
                ))
                .toList();

        return new TodayTraining(student.getStudentCode(), plan.getEncouragement(), tasks);
    }

    private TrainingPlanEntity createDefaultPlan(StudentEntity student, LocalDate today) {
        TrainingPlanEntity plan = trainingPlanRepository.save(new TrainingPlanEntity(
                student,
                today,
                "今天也一起完成 10 分钟练习"
        ));

        trainingTaskRepository.saveAll(List.of(
                new TrainingTaskEntity(plan, "task-1", "呼吸放松", "3 分钟", "pending", 1),
                new TrainingTaskEntity(plan, "task-2", "情绪日记", "1 次", "pending", 2),
                new TrainingTaskEntity(plan, "task-3", "正念专注", "5 分钟", "pending", 3)
        ));

        return plan;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "completed";
        }
        return "completed".equals(status) ? "completed" : "pending";
    }

    public record TodayTraining(
            String studentId,
            String encouragement,
            List<TrainingTaskData> tasks
    ) {
    }

    public record TrainingTaskData(
            String id,
            String title,
            String duration,
            String status
    ) {
    }
}
