package com.letmeknow.repository.school;

import com.letmeknow.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long>, SchoolRepositoryQueryDsl {
    Optional<School> findBySchoolName(String schoolName);
    List<School> findWithCollege();
}
