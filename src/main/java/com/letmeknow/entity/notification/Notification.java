package com.letmeknow.entity.notification;

import com.letmeknow.dto.NotificationDtoWithArticleDto;
import com.letmeknow.entity.Article;
import com.letmeknow.entity.BaseEntity;
import com.letmeknow.entity.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Notification extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "MEMBER_ID", insertable = false, updatable = false)
    private Member member;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ARTICLE_ID")
    private Article article;

    @NotNull
    private Boolean isRead;

    @Builder
    protected Notification(Long memberId, Article article) {
        this.memberId = memberId;
        this.article = article;
        this.article.addNotification(this);

        this.isRead = false;
    }

    //== Dto ==//
    public NotificationDtoWithArticleDto toDto() {
        return NotificationDtoWithArticleDto.builder()
            .id(this.id)
            .memberId(this.memberId)
            .articleDto(this.article.toDto())
            .isRead(this.isRead)
            .build();
    }
}
