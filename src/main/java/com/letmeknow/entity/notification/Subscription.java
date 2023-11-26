package com.letmeknow.entity.notification;

import com.letmeknow.entity.Board;
import com.letmeknow.entity.member.Member;
import com.letmeknow.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUBSCRIPTION_ID")
    private long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "BOARD_ID")
    private Board board;

    @Builder
    protected Subscription(Member member, Board board) {
        this.member = member;
        this.board = board;

        member.getSubscriptions().add(this);
        board.getSubscriptions().add(this);
    }

    // 연관관계 편의 메소드
    public void removeSubscription() {
        this.member.getSubscriptions().remove(this);
        this.board.getSubscriptions().remove(this);
    }
}
