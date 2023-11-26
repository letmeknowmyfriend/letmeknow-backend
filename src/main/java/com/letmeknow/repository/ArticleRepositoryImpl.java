package com.letmeknow.repository;

import com.letmeknow.entity.Article;
import com.letmeknow.entity.QArticle;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .selectFrom(QArticle.article)
                .where(QArticle.article.boardNumber.id.eq(boardId).and(QArticle.article.isNotice.eq(isNotice)))
                .orderBy(QArticle.article.id.desc())
                .limit(limit)
                .fetch();
    }
}
