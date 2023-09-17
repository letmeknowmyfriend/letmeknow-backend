package com.letmeknow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import com.letmeknow.domain.member.Member;
import com.letmeknow.dto.store.StoreDto;
import com.letmeknow.enumstorage.status.StoreStatus;
import com.letmeknow.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Table(name = "STORES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STORE_ID")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    @Embedded
    private Address address;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StoreStatus storeStatus = StoreStatus.CLOSE;

    @Builder
    protected Store(Member member, String name, String city, String street, String zipcode) {
        this.member = member;
        this.name = name;
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
            .build();
        this.storeStatus = StoreStatus.CLOSE;

        member.setStore(this);
    }

    public void updateStoreName(String newStoreName) {
        this.name = newStoreName;
    }

    public void updateStoreAddress(String city, String street, String zipcode) {
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
                .build();
    }

    public void changeStoreStatus(StoreStatus storeStatus) {
        this.storeStatus = storeStatus;
    }

    //==연관관계 메소드==//

    //==DTO==//
    public StoreDto toStoreDto() {
        return StoreDto.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .name(this.name)
                .address(this.address)
                .storeStatus(this.storeStatus)
            .build();
    }
}
