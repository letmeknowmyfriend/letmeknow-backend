package com.letmeknow.dto.member;

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
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String zipcode;

    @NotBlank
    private String status;

    @NotNull
    private Set<Long> jwtIds;

    @NotNull
    private int logInAttempt;

    private String passwordVerificationCode;

    @Builder
    protected MemberFindDto(long id, String name, String email, String city, String street, String zipcode, String status, Set<Long> jwtIds, int logInAttempt, String passwordVerificationCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
        this.status = status;
        this.jwtIds = jwtIds;
        this.logInAttempt = logInAttempt;
        this.passwordVerificationCode = passwordVerificationCode;
    }
}
