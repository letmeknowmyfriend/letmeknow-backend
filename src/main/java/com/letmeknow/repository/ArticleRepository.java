package com.letmeknow.repository;

import com.letmeknow.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryQueryDsl {
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice);
}
