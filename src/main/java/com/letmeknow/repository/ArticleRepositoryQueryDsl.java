package com.letmeknow.repository;

import com.letmeknow.entity.Article;

import java.util.List;

public interface ArticleRepositoryQueryDsl {
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice);
}
