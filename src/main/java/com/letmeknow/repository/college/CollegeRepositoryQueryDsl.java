package com.letmeknow.repository.college;

import com.letmeknow.entity.College;

import java.util.List;

public interface CollegeRepositoryQueryDsl {
    List<College> findBySchoolId(Long schoolId);
}
