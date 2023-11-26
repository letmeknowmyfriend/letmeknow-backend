package com.letmeknow.dto.member;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPasswordUpdateDto {
    @NotBlank
    private String password;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String newPasswordAgain;

    @Builder
    public MemberPasswordUpdateDto(String password, String newPassword, String newPasswordAgain) {
        this.password = password;
        this.newPassword = newPassword;
        this.newPasswordAgain = newPasswordAgain;
    }
}
