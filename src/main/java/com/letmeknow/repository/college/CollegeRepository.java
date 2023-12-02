package com.letmeknow.repository.college;

import com.letmeknow.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Long>, CollegeRepositoryQueryDsl {
    List<College> findBySchoolId(Long schoolId);
}
