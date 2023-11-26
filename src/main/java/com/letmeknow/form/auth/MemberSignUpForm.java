package com.letmeknow.form.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MemberSignUpForm {
    @NotBlank
    private final String name;
    @NotBlank
    @Email
    private final String email;
    @NotBlank
    private final String password;
    @NotBlank
    private final String passwordAgain;
    @NotBlank
    private final String city;
    @NotBlank
    private final String street;
    @NotBlank
    private final String zipcode;
}
