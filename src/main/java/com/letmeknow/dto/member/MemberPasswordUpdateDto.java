package com.letmeknow.dto.member;

import com.letmeknow.exception.member.NewPasswordNotMatchException;
import com.letmeknow.exception.member.InvalidPasswordException;
import com.letmeknow.message.MessageMaker;
import com.letmeknow.util.Validator;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

import static com.letmeknow.message.reason.MemberReason.NEW_PASSWORD;
import static com.letmeknow.message.Message.NOT_EQUAL;

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
    @QueryProjection
    public MemberPasswordUpdateDto(String password, String newPassword, String newPasswordAgain) throws NewPasswordNotMatchException, InvalidPasswordException {
        this.password = password;
        this.newPassword = newPassword;
        this.newPasswordAgain = newPasswordAgain;
    }
}
