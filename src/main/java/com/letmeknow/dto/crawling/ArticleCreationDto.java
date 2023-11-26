package com.letmeknow.dto.crawling;

import com.letmeknow.entity.Article;
import com.letmeknow.entity.Board;
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
    private long link;

    @NotBlank
    private String createdAt;

    @NotNull
    private Boolean isNotice;

    @Builder
    protected ArticleCreationDto(long boardId, String title, long link, String createdAt, Boolean isNotice) {
        this.boardId = boardId;
        this.title = title;
        this.link = link;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }

    // toEntity
    public Article toEntity(Board boardNumber) {
        return Article.builder()
                .boardNumber(boardNumber)
                .title(title)
                .link(link)
                .createdAt(createdAt)
                .isNotice(isNotice)
            .build();
    }

}
