package com.letmeknow.form.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class MemberSignUpForm {
    private String name;
    private String email;
    private String password;
    private String passwordAgain;
    private String city;
    private String street;
    private String zipcode;
}
