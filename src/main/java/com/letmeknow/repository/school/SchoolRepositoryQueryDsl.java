package com.letmeknow.repository.school;

import com.letmeknow.entity.School;

import java.util.List;

public interface SchoolRepositoryQueryDsl {
    List<School> findWithCollege();
}
