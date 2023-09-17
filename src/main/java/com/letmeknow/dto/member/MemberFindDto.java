package com.letmeknow.dto.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberFindDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String email;
    private String city;
    private String street;
    private String zipcode;
    private String status;
    private Long jwtId;
    private int logInAttempt;
    private String passwordVerificationCode;

    @Builder
    protected MemberFindDto(Long id, String name, String email, String city, String street, String zipcode, String status, Long jwtId, int logInAttempt, String passwordVerificationCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.status = status;
        this.jwtId = jwtId;
        this.logInAttempt = logInAttempt;
        this.passwordVerificationCode = passwordVerificationCode;
    }
}
