package com.letmeknow.repository.college;

import com.letmeknow.entity.College;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.letmeknow.entity.QCollege.college;

@Repository
@RequiredArgsConstructor
public class CollegeRepositoryImpl implements CollegeRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<College> findBySchoolId(Long schoolId) {
        JPAQueryFactory query = new JPAQueryFactory(em);

        return query.selectFrom(college)
            .where(college.school.id.eq(schoolId))
            .fetch();
    }
}
