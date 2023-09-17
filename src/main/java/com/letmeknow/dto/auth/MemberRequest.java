package com.letmeknow.dto.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
public class MemberRequest {
    @NotBlank
    private final String email;
    @NotBlank
    private final String password;
}
