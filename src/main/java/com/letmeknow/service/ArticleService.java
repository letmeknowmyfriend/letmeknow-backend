package com.letmeknow.service;

import com.letmeknow.domain.Article;
import com.letmeknow.domain.Board;
import com.letmeknow.dto.crawling.ArticleCreationDto;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.repository.ArticleRepository;
import com.letmeknow.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final BoardRepository boardNumberRepository;

    public List<Article> findByBoardNumberOrderByIdDesc(Long boardNumber, Pageable pageable) {
        return articleRepository.findByBoardNumberOrderByIdDesc(boardNumber, pageable);
    }

    public List<ArticleDto> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(Long boardId, Long limit, Boolean isNotice) {
        return articleRepository.findAllByBoardIdAndIsNoticeOrderByIdDescLimit(boardId, limit, isNotice).stream()
                .map(Article::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAllArticles(List<ArticleCreationDto> articleCreationDtos) {
        if (articleCreationDtos.isEmpty()) {
            return;
        }

        // boardNumber를 가져온다.
        Board boardNumber = boardNumberRepository.findOneById(articleCreationDtos.get(0).getBoardId())
                .orElseThrow();

        List<Article> articles = articleCreationDtos.stream()
                .map(articleCreationDto -> Article.builder()
                        .boardNumber(boardNumber)
                        .title(articleCreationDto.getTitle())
                        .link(articleCreationDto.getLink())
                        .createdAt(articleCreationDto.getCreatedAt())
                        .isNotice(articleCreationDto.getIsNotice())
                        .build())
                .collect(Collectors.toList());

        articleRepository.saveAll(articles);
    }
}
