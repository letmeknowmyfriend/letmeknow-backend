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

    @JoinColumn(name = "BOARD_ID", insertable = false, updatable = false)
    @ManyToOne(fetch = LAZY, targetEntity = Board.class)
    private Board board;

    @Column(name = "BOARD_ID", nullable = false)
    private Long boardId;

    @NotBlank
    private String title;

    @NotNull
    private long link;

    @NotBlank
    private String createdAt;

    @NotNull
    private boolean isNotice;

    @NotNull
    @OneToMany(mappedBy = "article")
    private List<Notification> notifications = new ArrayList<>();

    @Builder
    protected Article(Long boardId, String title, long link, String createdAt, Boolean isNotice) {
        this.boardId = boardId;
        this.title = title;
        this.link = link;
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
                .title(this.title)
                .link(this.link)
                .createdAt(this.createdAt)
                .isNotice(this.isNotice)
            .build();
    }
}
