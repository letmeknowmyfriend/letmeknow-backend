package com.letmeknow.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(force = true)
public class SignInAPIRequest {
    @NotBlank
    private final String email;
    @NotBlank
    private final String password;
}
