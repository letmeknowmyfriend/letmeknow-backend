package com.letmeknow.dto.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SignInAPIRequest {
    @NotBlank
    private final String email;
    @NotBlank
    private final String password;
}
