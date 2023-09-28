package com.letmeknow.domain.notification;

import com.letmeknow.domain.member.Member;
import com.letmeknow.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEVICE_TOKEN_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @NotBlank
    @Column(unique = true)
    private String deviceToken;

    @Builder
    protected DeviceToken(Member member, String deviceToken) {
        this.member = member;
        this.deviceToken = deviceToken;

        member.getDeviceTokens().add(this);
    }
}
