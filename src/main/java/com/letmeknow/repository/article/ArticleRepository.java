package com.letmeknow.repository.article;

import com.letmeknow.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryQueryDsl {
    List<Article> findByNoOffset(Long boardId, Long lastId, Long pageSize);
    List<Article> findByNoOffsetWithKeyword(Long boardId, String keyword, Long lastId, Long pageSize);
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice);
    void saveAllArticles(List<Article> articles);
}
