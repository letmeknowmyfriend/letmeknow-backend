package com.letmeknow.repository;

import com.letmeknow.domain.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryQueryDsl {
    List<Article> findByBoardNumberOrderByIdDesc(Long boardNumber, Pageable pageable);
    List<Article> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(Long boardId, Long limit, Boolean isNotice);
}
