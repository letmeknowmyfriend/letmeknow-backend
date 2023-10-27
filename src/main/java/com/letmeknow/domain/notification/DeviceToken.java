package com.letmeknow.domain.notification;

import com.letmeknow.domain.auth.RefreshToken;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.notification.DeviceTokenDto;
import com.letmeknow.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    @NotNull
    @OneToMany(mappedBy = "deviceToken")
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @Builder
    protected DeviceToken(Member member, String deviceToken) {
        this.member = member;
        this.deviceToken = deviceToken;

        member.getDeviceTokens().add(this);
    }

    // 연관관계 편의 메서드
    public void addJwt(RefreshToken refreshToken) {
        this.refreshTokens.add(refreshToken);
    }

    public void removeJwt(RefreshToken refreshToken) {
        this.refreshTokens.remove(refreshToken);
    }

    // DTO
    public DeviceTokenDto toDeviceTokenDto() {
        return DeviceTokenDto.builder()
            .id(this.id)
            .memberId(this.member.getId())
            .deviceToken(this.deviceToken)
            .jwtIds(this.refreshTokens.stream().map(RefreshToken::getId).collect(Collectors.toSet()))
            .build();
    }
}
