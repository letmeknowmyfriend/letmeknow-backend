package com.letmeknow.repository;

import com.letmeknow.domain.Article;

import java.util.List;

public interface ArticleRepositoryQueryDsl {
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(Long boardId, Long limit, Boolean isNotice);
}
