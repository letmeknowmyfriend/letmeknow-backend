package com.letmeknow.entity.notification;

import com.letmeknow.dto.NotificationDto;
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

    @Column(name = "MEMBER_ID")
    private Long memberId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ARTICLE_ID")
    private Article article;

    @NotNull
    private Boolean isRead;

    @Builder
    protected Notification(Member member, Article article) {
        this.member = member;
        this.article = article;
        this.article.addNotification(this);

        this.isRead = false;
    }

    @Builder(builderMethodName = "buildNotificationWithMemberId")
    protected Notification(Long memberId, Article article) {
        this.memberId = memberId;
        this.article = article;
        this.article.addNotification(this);

        this.isRead = false;
    }

    //== Dto ==//
    public NotificationDto toDto() {
        return NotificationDto.builder()
            .id(this.id)
            .memberId(this.memberId)
            .articleId(this.article.getId())
            .isRead(this.isRead)
            .build();
    }
}
