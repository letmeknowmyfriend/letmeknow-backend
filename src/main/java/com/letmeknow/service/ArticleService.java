package com.letmeknow.service;

import com.letmeknow.entity.Article;
import com.letmeknow.entity.Board;
import com.letmeknow.dto.crawling.ArticleCreationDto;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.repository.ArticleRepository;
import com.letmeknow.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public List<ArticleDto> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice) {
        return articleRepository.findAllByBoardIdAndIsNoticeOrderByIdDescLimit(boardId, limit, isNotice).stream()
                .map(Article::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAllArticles(List<Article> articles) {
        if (articles.isEmpty()) {
            return;
        }

        articleRepository.saveAll(articles);
    }
}
