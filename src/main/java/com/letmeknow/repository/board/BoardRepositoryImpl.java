package com.letmeknow.repository.board;

import com.letmeknow.entity.Board;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.letmeknow.entity.QBoard.board;
import static com.letmeknow.entity.notification.QSubscription.subscription;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryQueryDsl {
    private final EntityManager em;
}
