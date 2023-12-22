package com.letmeknow.dto.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED, force = true)
public class MemberChangePasswordDto {
    @NotBlank
    String email;

    @Builder
    protected MemberChangePasswordDto(String email) {
        this.email = email;
    }
}
