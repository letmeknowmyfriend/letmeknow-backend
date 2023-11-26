package com.letmeknow.dto.temporarymember;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class TemporaryMemberDto {
    private final long id;
    private final String name;
    private final String email;
    private final String city;
    private final String street;
    private final String zipcode;
    private final String verificationCode;

    @Builder
    protected TemporaryMemberDto(long id, String name, String email, String city, String street, String zipcode, String verificationCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.verificationCode = verificationCode;
    }
}
