package com.letmeknow.domain.auth;

import com.letmeknow.domain.notification.DeviceToken;
import com.letmeknow.dto.jwt.JwtFindDtoWithDeviceToken;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.jwt.JwtFindDto;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String refreshToken;

    @NotNull
    private LocalDateTime expiredAt;

    @NotNull
    @ManyToOne(fetch = LAZY)
    private Member member;

    @NotNull
    @ManyToOne(fetch = LAZY)
    private DeviceToken deviceToken;

    @Builder
    protected RefreshToken(String refreshToken, Member member, DeviceToken deviceToken) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
        this.member = member;
        this.deviceToken = deviceToken;

        member.addJwt(this);
        deviceToken.addJwt(this);
    }

    //== 비즈니스 로직 ==//
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
    }

    public void deleteRefreshToken() {
        member.removeJwt(this);
        deviceToken.removeJwt(this);
    }

    //== Dto ==//
    public JwtFindDto toJwtFindDto() {
        return JwtFindDto.builder()
                .id(id)
                .refreshToken(refreshToken)
                .expiredAt(expiredAt)
                .memberId(member.getId())
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    // DTO
    public JwtFindDtoWithDeviceToken toJwtFindDtoWithDeviceToken() {
        return JwtFindDtoWithDeviceToken.builder()
                .id(id)
                .refreshToken(refreshToken)
                .expiredAt(expiredAt)
                .memberId(member.getId())
                .deviceTokenDto(deviceToken.toDeviceTokenDto())
                .build();
    }
}
