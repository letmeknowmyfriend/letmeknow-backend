package com.letmeknow.form.auth;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
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
    @NotNull
    private final String city;
    @NotNull
    private final String street;
    @NotNull
    private final String zipcode;

    @Builder
    protected MemberSignUpForm(String name, String email, String password, String passwordAgain, String city, String street, String zipcode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordAgain = passwordAgain;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
