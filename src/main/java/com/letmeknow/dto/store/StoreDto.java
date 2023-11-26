package com.letmeknow.dto.store;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import com.letmeknow.entity.Address;
import com.letmeknow.dto.address.AddressDto;
import com.letmeknow.enumstorage.status.StoreStatus;

@Getter
public class StoreDto {

    private long id;

    private long memberId;

    private String name;

    private AddressDto addressDto;

    private String storeStatus;

    @Builder
    @QueryProjection
    public StoreDto(long id, long memberId, String name, Address address, StoreStatus storeStatus) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.addressDto = AddressDto.builder()
                .city(address.getCity())
                .street(address.getStreet())
                .zipcode(address.getZipcode())
                .build();
        this.storeStatus = storeStatus.toString();
    }
}
