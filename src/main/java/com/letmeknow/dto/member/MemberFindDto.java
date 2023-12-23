package com.letmeknow.dto.member;

import com.letmeknow.dto.address.AddressDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberFindDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String city;

    @NotNull
    private AddressDto address;

    @NotNull
    private Boolean consentToReceivePushNotification;

    @Builder
    protected MemberFindDto(Long id, String name, String email, String city, AddressDto address, Boolean consentToReceivePushNotification) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.address = address;
        this.consentToReceivePushNotification = consentToReceivePushNotification;
    }
}
