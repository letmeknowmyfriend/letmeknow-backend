package com.letmeknow.dto.jwt;

import com.letmeknow.dto.notification.DeviceTokenDto;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class JwtFindDtoWithDeviceToken {
    @NotNull
    private long id;

    @NotBlank
    private String refreshToken;

    @NotNull
    private LocalDateTime expiredAt;

    @NotNull
    private long memberId;

    private DeviceTokenDto deviceTokenDto;

    @Builder
    protected JwtFindDtoWithDeviceToken(long id, String refreshToken, LocalDateTime expiredAt, long memberId, DeviceTokenDto deviceTokenDto) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
        this.memberId = memberId;
        this.deviceTokenDto = deviceTokenDto;
    }
}
