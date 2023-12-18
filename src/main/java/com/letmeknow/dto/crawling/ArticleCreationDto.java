package com.letmeknow.dto.crawling;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ArticleCreationDto {
    @NotNull
    private long boardId;

    @NotBlank
    private String title;

    @NotNull
    private long articleId;

    @NotBlank
    private String createdAt;

    @NotNull
    private Boolean isNotice;

    @Builder
    protected ArticleCreationDto(long boardId, String title, long articleId, String createdAt, Boolean isNotice) {
        this.boardId = boardId;
        this.title = title;
        this.articleId = articleId;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }
}
