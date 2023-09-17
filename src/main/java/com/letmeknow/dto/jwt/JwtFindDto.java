package com.letmeknow.dto.jwt;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class JwtFindDto {
    @NotNull
    private Long id;

    @NotBlank
    private String refreshToken;

    @NotNull
    private LocalDateTime expiredAt;

    @NotNull
    private Long memberId;

    @Builder
    protected JwtFindDto(Long id, String refreshToken, LocalDateTime expiredAt, Long memberId) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
        this.memberId = memberId;
    }
}
