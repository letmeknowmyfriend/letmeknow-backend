package com.security.form.auth;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class MemberSignUpForm {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String email;

    private String password;

    private String passwordAgain;

    @NotBlank(message = "도시는 필수입니다.")
    private String city;

    @NotBlank(message = "도로명은 필수입니다.")
    private String street;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;
}
