package com.mindcare.app.student;

import com.mindcare.app.school.SchoolClassEntity;
import com.mindcare.app.school.SchoolClassRepository;
import com.mindcare.app.school.SchoolEntity;
import com.mindcare.app.school.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private static final String DEFAULT_SCHOOL_CODE = "DEMO";
    private static final String DEFAULT_SCHOOL_NAME = "示范中学";
    private static final String DEFAULT_CLASS_NAME = "七年级2班";

    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;

    public StudentService(
            SchoolRepository schoolRepository,
            SchoolClassRepository schoolClassRepository,
            StudentRepository studentRepository
    ) {
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentEntity upsertStudent(String schoolCode, String studentCode, String studentName) {
        return upsertStudent(schoolCode, studentCode, studentName, DEFAULT_CLASS_NAME, DEFAULT_SCHOOL_NAME);
    }

    @Transactional
    public StudentEntity upsertStudent(
            String schoolCode,
            String studentCode,
            String studentName,
            String className,
            String schoolName
    ) {
        String normalizedSchoolCode = normalize(schoolCode, DEFAULT_SCHOOL_CODE);
        String normalizedStudentCode = normalize(studentCode, "student-demo");
        String normalizedStudentName = normalize(studentName, "同学");
        String normalizedClassName = normalize(className, DEFAULT_CLASS_NAME);
        String normalizedSchoolName = normalize(schoolName, DEFAULT_SCHOOL_NAME);

        SchoolEntity school = schoolRepository.findBySchoolCode(normalizedSchoolCode)
                .orElseGet(() -> schoolRepository.save(new SchoolEntity(normalizedSchoolCode, normalizedSchoolName)));
        school.setSchoolName(normalizedSchoolName);

        SchoolClassEntity schoolClass = schoolClassRepository.findBySchoolAndClassName(school, normalizedClassName)
                .orElseGet(() -> schoolClassRepository.save(new SchoolClassEntity(school, normalizedClassName)));

        StudentEntity student = studentRepository.findBySchool_SchoolCodeAndStudentCode(normalizedSchoolCode, normalizedStudentCode)
                .orElseGet(() -> new StudentEntity(school, schoolClass, normalizedStudentCode, normalizedStudentName));
        student.updateProfile(school, schoolClass, normalizedStudentName);

        return studentRepository.save(student);
    }

    @Transactional
    public StudentEntity findOrCreatePlaceholder(String studentCode) {
        String normalizedStudentCode = normalize(studentCode, "student-demo");
        return studentRepository.findFirstByStudentCodeOrderByUpdatedAtDesc(normalizedStudentCode)
                .orElseGet(() -> upsertStudent(DEFAULT_SCHOOL_CODE, normalizedStudentCode, "林同学"));
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
