package com.letmeknow.service;

import com.letmeknow.dto.crawling.ArticleCreationDto;
import com.letmeknow.entity.Article;
import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.entity.Board;
import com.letmeknow.exception.NoSuchBoardException;
import com.letmeknow.repository.article.ArticleRepository;
import com.letmeknow.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.letmeknow.message.messages.BoardMessages.BOARD;
import static com.letmeknow.message.messages.Messages.NOT_EXISTS;
import static com.letmeknow.message.messages.Messages.SUCH;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public List<ArticleDto> findAllByBoardIdAndIsNoticeOrderByIdDescLimit(long boardId, long limit, Boolean isNotice) {
        return articleRepository.findAllByBoardIdAndIsNoticeOrderByIdDescLimit(boardId, limit, isNotice).stream()
                .map(Article::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Article> saveAllArticles(Long boardId, List<ArticleCreationDto> articles) {
        List<Article> newArticles = new ArrayList<>();

        for (ArticleCreationDto article : articles) {
            Article newArticle = Article.builder()
                .boardId(boardId)
                .title(article.getTitle())
                .link(article.getLink())
                .createdAt(article.getCreatedAt())
                .isNotice(article.getIsNotice())
                .build();

            newArticles.add(newArticle);
        }

        return articleRepository.saveAll(newArticles);
    }
}
