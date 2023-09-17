package com.security.domain.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.security.domain.member.Member;
import com.security.dto.jwt.JwtFindDto;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Jwt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String refreshToken;

    @NotNull
    private LocalDateTime expiredAt;

    @OneToOne(fetch = LAZY)
    private Member member;

    @Builder
    protected Jwt(String refreshToken, Member member) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
        this.member = member;
        member.setJwt(this);
    }

    //== 비즈니스 로직 ==//
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.expiredAt = LocalDateTime.now().plusDays(7);
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
}
