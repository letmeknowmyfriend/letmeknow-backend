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
    private long id;

    @NotNull
    private long boardId;

    @NotBlank
    private String title;

    @NotNull
    private long link;

    @NotBlank
    private String createdAt;

    @NotNull
    private Boolean isNotice;

    @Builder
    protected ArticleDto(long id, long boardId, String title, long link, String createdAt, Boolean isNotice) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.link = link;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }
}
