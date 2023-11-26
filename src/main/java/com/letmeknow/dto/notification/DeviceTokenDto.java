package com.letmeknow.dto.notification;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
public class DeviceTokenDto {
    @NotNull
    private long id;

    @NotNull
    private long memberId;

    @NotBlank
    private String deviceToken;

    @NotNull
    private Long refreshTokenId;

    @Builder
    protected DeviceTokenDto(long id, long memberId, String deviceToken, long refreshTokenId) {
        this.id = id;
        this.memberId = memberId;
        this.deviceToken = deviceToken;
        this.refreshTokenId = refreshTokenId;
    }
}
