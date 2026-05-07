package com.mindcare.app.auth;

import com.mindcare.app.common.api.ApiResponse;
import com.mindcare.app.consent.ConsentService;
import com.mindcare.app.student.StudentEntity;
import com.mindcare.app.student.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final StudentService studentService;
    private final ConsentService consentService;

    public AuthController(StudentService studentService, ConsentService consentService) {
        this.studentService = studentService;
        this.consentService = consentService;
    }

    @PostMapping("/student/login")
    public ApiResponse<StudentLoginPayload> studentLogin(@Valid @RequestBody StudentLoginRequest request) {
        StudentEntity student = studentService.upsertStudent(request.schoolCode(), request.studentId(), request.studentName());
        ConsentService.ConsentStatus consentStatus = consentService.getStatus(student.getStudentCode());
        return ApiResponse.success(new StudentLoginPayload(
                "demo-" + UUID.randomUUID(),
                "student",
                !consentStatus.allRequiredCompleted(),
                consentStatus.nextStep(),
                new StudentProfile(
                        student.getStudentCode(),
                        student.getStudentName(),
                        student.getSchoolClass().getClassName()
                ),
                new SchoolProfile(
                        student.getSchool().getSchoolName(),
                        student.getSchool().getSchoolCode()
                )
        ));
    }

    @PostMapping("/guardian/bind")
    public ApiResponse<GuardianBindPayload> guardianBind(@Valid @RequestBody GuardianBindRequest request) {
        return ApiResponse.success(new GuardianBindPayload(
                request.inviteCode(),
                "绑定成功",
                "林同学",
                "父母监护人"
        ));
    }

    public record StudentLoginRequest(
            @NotBlank String schoolCode,
            @NotBlank String studentId,
            @NotBlank String studentName
    ) {
    }

    public record StudentLoginPayload(
            String sessionToken,
            String role,
            boolean consentRequired,
            String nextStep,
            StudentProfile student,
            SchoolProfile school
    ) {
    }

    public record StudentProfile(
            String studentId,
            String studentName,
            String className
    ) {
    }

    public record SchoolProfile(
            String schoolName,
            String schoolCode
    ) {
    }

    public record GuardianBindRequest(
            @NotBlank String inviteCode,
            @NotBlank String guardianName
    ) {
    }

    public record GuardianBindPayload(
            String inviteCode,
            String status,
            String studentName,
            String relationship
    ) {
    }
}
