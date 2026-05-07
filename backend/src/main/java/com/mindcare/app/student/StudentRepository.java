package com.mindcare.app.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findBySchool_SchoolCodeAndStudentCode(String schoolCode, String studentCode);

    Optional<StudentEntity> findFirstByStudentCodeOrderByUpdatedAtDesc(String studentCode);
}
