package com.letmeknow.dto.crawling;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class ArticleDto {
    @NotNull
    private Long id;

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
    protected ArticleDto(Long id, Long boardId, String title, Long link, String createdAt, Boolean isNotice) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.link = link;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }
}
