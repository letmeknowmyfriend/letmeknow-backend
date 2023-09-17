package com.letmeknow.dto.crawling;

import com.letmeknow.domain.Article;
import com.letmeknow.domain.BoardNumber;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ArticleCreationDto {
    @NotNull
    private Long boardId;

    @NotBlank
    private String title;

    @NotNull
    private Long link;

    @NotBlank
    private String createdAt;

    @NotNull
    private Boolean isNotice;

    @Builder
    protected ArticleCreationDto(Long boardId, String title, Long link, String createdAt, Boolean isNotice) {
        this.boardId = boardId;
        this.title = title;
        this.link = link;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }

    // toEntity
    public Article toEntity(BoardNumber boardNumber) {
        return Article.builder()
                .boardNumber(boardNumber)
                .title(title)
                .link(link)
                .createdAt(createdAt)
                .isNotice(isNotice)
            .build();
    }

}
