package com.letmeknow.dto.store;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreUpdateDto {
    @NotNull
    private long id;
    @NotNull
    private long memberId;
    @NotBlank
    private String name;
    @NotBlank
    private String city;
    @NotBlank
    private String street;
    @NotBlank
    private String zipcode;
    @NotBlank
    private String storeStatus;

    @Builder
    protected StoreUpdateDto(long id, long memberId, String name, String city, String street, String zipcode, String storeStatus) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.storeStatus = storeStatus;
    }
}
