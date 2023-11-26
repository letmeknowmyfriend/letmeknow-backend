package com.letmeknow.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.letmeknow.entity.member.Member;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2 {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    private Member member;

    @NotBlank
    private String registrationId;

    @NotBlank
    private String providerId;

    @Builder
    protected OAuth2(Member member, String registrationId, String providerId) {
        this.member = member;
        this.registrationId = registrationId;
        this.providerId = providerId;

        //양방향 연관관계 설정
        member.addOAuth2(this);
    }
}
