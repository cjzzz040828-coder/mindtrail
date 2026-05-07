package com.mindcare.app.school;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClassEntity, Long> {

    Optional<SchoolClassEntity> findBySchoolAndClassName(SchoolEntity school, String className);
}
