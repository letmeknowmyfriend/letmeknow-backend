package com.security.dto.member;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberPasswordUpdateDto {

    private Long id;

    private String password;

    private String newPassword;

    @Builder
    @QueryProjection
    public MemberPasswordUpdateDto(Long id, String password, String newPassword) {
        this.id = id;
        this.password = password;
        this.newPassword = newPassword;
    }
}
