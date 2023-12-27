package com.letmeknow.dto;

import com.letmeknow.dto.crawling.ArticleDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class NotificationDtoWithBoardViewUrlAndArticleDto {
    private final Long id;
    private final Long memberId;
    private final String boardName;
    private final String boardViewUrl;
    private final ArticleDto articleDto;
    private final Boolean isRead;

    @Builder
    protected NotificationDtoWithBoardViewUrlAndArticleDto(Long id, Long memberId, String boardName, String boardViewUrl, ArticleDto articleDto, Boolean isRead) {
        this.id = id;
        this.memberId = memberId;
        this.boardName = boardName;
        this.boardViewUrl = boardViewUrl;
        this.articleDto = articleDto;
        this.isRead = isRead;
    }
}
