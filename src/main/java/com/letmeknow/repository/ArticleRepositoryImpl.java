package com.letmeknow.repository;

import com.letmeknow.domain.Article;
import com.letmeknow.domain.QArticle;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(Long boardId, Long limit, Boolean isNotice) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .selectFrom(QArticle.article)
                .where(QArticle.article.boardNumber.id.eq(boardId).and(QArticle.article.isNotice.eq(isNotice)))
                .orderBy(QArticle.article.id.desc())
                .limit(limit)
                .fetch();
    }
}
