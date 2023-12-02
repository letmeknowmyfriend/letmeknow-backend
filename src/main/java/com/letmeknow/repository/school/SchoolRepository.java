package com.letmeknow.repository.school;

import com.letmeknow.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long>, SchoolRepositoryQueryDsl {
    List<School> findWithCollege();
}
