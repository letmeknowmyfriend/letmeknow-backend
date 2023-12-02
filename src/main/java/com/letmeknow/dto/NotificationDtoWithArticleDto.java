package com.letmeknow.dto;

import com.letmeknow.dto.crawling.ArticleDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class NotificationDtoWithArticleDto {
    private final Long id;
    private final Long memberId;
    private final ArticleDto articleDto;
    private final Boolean isRead;

    @Builder
    protected NotificationDtoWithArticleDto(Long id, Long memberId, ArticleDto articleDto, Boolean isRead) {
        this.id = id;
        this.memberId = memberId;
        this.articleDto = articleDto;
        this.isRead = isRead;
    }
}
