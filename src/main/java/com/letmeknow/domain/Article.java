package com.letmeknow.domain;

import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ARTICLE_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "BOARD_ID")
    private BoardNumber boardNumber;

    @NotBlank
    private String title;

    @NotNull
    private Long link;

    @NotBlank
    private String createdAt;

    @NotNull
    private boolean isNotice;

    @Builder
    protected Article(BoardNumber boardNumber, String title, Long link, String createdAt, Boolean isNotice) {
        this.boardNumber = boardNumber;
        this.title = title;
        this.link = link;
        this.createdAt = createdAt;
        this.isNotice = isNotice;

        boardNumber.addArticle(this);
    }

    // Dto
    public ArticleDto toDto() {
        return ArticleDto.builder()
                .id(this.id)
                .boardId(this.boardNumber.getId())
                .title(this.title)
                .link(this.link)
                .createdAt(this.createdAt)
                .isNotice(this.isNotice)
            .build();
    }
}
