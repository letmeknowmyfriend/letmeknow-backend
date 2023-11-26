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
    private long id;
    @NotNull
    private long memberId;
    @Builder
    protected StoreToggleStatusDto(long id, long memberId) {
        this.id = id;
        this.memberId = memberId;
    }
}
