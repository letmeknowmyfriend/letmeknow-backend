package com.letmeknow.entity;

import com.letmeknow.dto.crawling.ArticleDto;
import com.letmeknow.entity.notification.Notification;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ARTICLE_ID")
    private long id;

    @Column(name = "BOARD_ID", nullable = false)
    private Long boardId;
    @JoinColumn(name = "BOARD_ID", insertable = false, updatable = false)
    @ManyToOne(fetch = LAZY, targetEntity = Board.class)
    private Board board;

    @NotBlank
    private String articleName;

    @NotNull
    private long articleLink;

    @NotBlank
    private String createdAt;

    @NotNull
    private boolean isNotice;

    @NotNull
    @OneToMany(mappedBy = "article")
    private List<Notification> notifications = new ArrayList<>();

    @Builder
    protected Article(Long boardId, String articleName, long articleLink, String createdAt, Boolean isNotice) {
        this.boardId = boardId;
        this.articleName = articleName;
        this.articleLink = articleLink;
        this.createdAt = createdAt;
        this.isNotice = isNotice;
    }

    //-- 연관관계 편의 메소드 --//
    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    // Dto
    public ArticleDto toDto() {
        return ArticleDto.builder()
                .id(this.id)
                .boardId(this.board.getId())
                .articleName(this.articleName)
                .articleLink(this.articleLink)
                .createdAt(this.createdAt)
                .isNotice(this.isNotice)
            .build();
    }
}
