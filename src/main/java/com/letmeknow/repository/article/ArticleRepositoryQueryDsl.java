package com.letmeknow.repository.article;

import com.letmeknow.entity.Article;

import java.util.List;

public interface ArticleRepositoryQueryDsl {
    List<Article> findByNoOffset(Long boardId, Long lastId, Long pageSize);
    List<Article> findByNoOffsetWithKeyword(Long boardId, String keyword, Long lastId, Long pageSize);
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice);
    void saveAllArticles(List<Article> articles);
}
