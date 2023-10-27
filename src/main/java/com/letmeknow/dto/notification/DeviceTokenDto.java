package com.letmeknow.dto.notification;

import com.letmeknow.domain.member.Member;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
public class DeviceTokenDto {
    @NotNull
    private Long id;

    @NotNull
    private Long memberId;

    @NotBlank
    private String deviceToken;

    @NotNull
    private Set<Long> jwtIds;

    @Builder
    protected DeviceTokenDto(Long id, Long memberId, String deviceToken, Set<Long> jwtIds) {
        this.id = id;
        this.memberId = memberId;
        this.deviceToken = deviceToken;
        this.jwtIds = jwtIds;
    }
}
