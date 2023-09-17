package com.letmeknow.dto.store;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreToggleStatusDto {
    @NotNull
    private Long id;
    @NotNull
    private Long memberId;
    @Builder
    protected StoreToggleStatusDto(Long id, Long memberId) {
        this.id = id;
        this.memberId = memberId;
    }
}
