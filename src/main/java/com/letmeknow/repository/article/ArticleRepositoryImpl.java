package com.letmeknow.repository.article;

import com.letmeknow.entity.Article;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.letmeknow.entity.QArticle.*;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryQueryDsl {
    private final EntityManager em;

    @Override
    public List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);

        return queryFactory
                .selectFrom(article)
                .where(article.board.id.eq(boardId).and(article.isNotice.eq(isNotice)))
                .orderBy(article.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public void saveAllArticles(List<Article> articles) {
        em.persist(articles);
    }
}
