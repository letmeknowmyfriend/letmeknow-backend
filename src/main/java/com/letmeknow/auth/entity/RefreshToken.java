package com.letmeknow.auth.entity;

import com.letmeknow.dto.jwt.JwtFindDto;
import com.letmeknow.entity.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(unique = true, columnDefinition = "varchar(300)")
    private String refreshToken;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @NotNull
    @OneToOne(mappedBy = "refreshToken")
    private DeviceToken deviceToken;

    @Builder
    protected RefreshToken(String refreshToken, Member member, DeviceToken deviceToken) {
        this.refreshToken = refreshToken;

        this.member = member;
        member.addDeviceToken(deviceToken);

        this.deviceToken = deviceToken;
        deviceToken.setRefreshToken(this);
    }

    //== 비즈니스 로직 ==//
    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }

    public void deleteRefreshToken() {
        member.removeRefreshToken(this);
    }

    //== Dto ==//w

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", refreshToken='" + refreshToken + '\'' +
                ", member=" + member +
                '}';
    }
}
