package com.letmeknow.repository.school;

import com.letmeknow.entity.School;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.letmeknow.entity.QSchool.school;

@Repository
@RequiredArgsConstructor
public class SchoolRepositoryImpl implements SchoolRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<School> findWithCollege() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
            .selectFrom(school)
            .leftJoin(school.colleges).fetchJoin()
            .fetch();
    }
}
