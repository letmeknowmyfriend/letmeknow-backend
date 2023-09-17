package com.security.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import com.letmeknow.domain.Store;
import com.security.domain.Address;
import com.security.domain.auth.Jwt;
import com.security.domain.auth.OAuth2;
import com.security.dto.member.MemberFindDto;
import com.security.entity.BaseEntity;
import com.security.enumstorage.role.MemberRole;
import com.security.enumstorage.status.MemberStatus;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    private String password;

    @Embedded
    private Address address;

    @JsonIgnore
    @NotNull
    @OneToMany(mappedBy = "member")
    private List<Store> stores = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @NotNull
    @OneToMany(mappedBy = "member")
    private List<OAuth2> oAuth2s = new ArrayList<>();

    @OneToOne(mappedBy = "member")
    private Jwt jwt;

    @NotNull
    private int logInAttempt = 0;

    private String passwordVerificationCode;

    @Builder
    protected Member(String name, String email, String password, String city, String street, String zipcode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
            .build();
        this.role = MemberRole.ADMIN;
        this.status = MemberStatus.ACTIVE;
    }

    @Builder
    protected Member(String email) {
        this.email = email;
        this.role = MemberRole.ADMIN;
        this.status = MemberStatus.ACTIVE;
    }

    //== 비즈니스 로직 ==//
    public void updateMemberName(String name) {
        this.name = name;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateMemberAddress(String city, String street, String zipcode) {
        this.address = Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
                .build();
    }

    public void unlock() {
        if (this.status == MemberStatus.LOCKED) {
            this.status = MemberStatus.ACTIVE;
            this.logInAttempt = 0;
        }
    }

    public MemberStatus countUpLogInAttempt() {
        this.logInAttempt += 1;

        if (this.logInAttempt >= 5) {
            if (this.status != MemberStatus.DELETED) {
                return this.status = MemberStatus.LOCKED;
            }
        }

        return this.status;
    }

    public void resetLogInAttempt() {
        this.logInAttempt = 0;
    }

    public void updatePasswordVerificationCode(String passwordVerificationCode) {
        this.passwordVerificationCode = passwordVerificationCode;
    }

    public void deletePasswordVerificationCode() {
        this.passwordVerificationCode = "";
    }

    //== 테스트 로직 ==//
    public void switchRole() {
        if (this.role == MemberRole.ADMIN) {
            this.role = MemberRole.MEMBER;
        } else {
            this.role = MemberRole.ADMIN;
        }
    }

    //==연관관계 메소드==//
    public void setStore(Store store) {
        stores.add(store);
    }

    public void addOAuth2(OAuth2 oAuth2) {
        oAuth2s.add(oAuth2);
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

//    public void updateStoreName(String originalName, String newStoreName) throws IllegalStateException {
//        stores.stream()
//                .filter(store -> store.getName().equals(originalName))
//                .findFirst()
//                .map(store -> store.updateStoreName(newStoreName))
//                .orElseThrow(() -> new IllegalStateException("가게 이름을 업데이트 할 수 없습니다."));
//    }

    //==DTO==//
    public MemberFindDto toMemberFindDto() {
        return MemberFindDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .city(address != null ? address.getCity() : null)
                .street(address != null ? address.getStreet() : null)
                .zipcode(address != null ? address.getZipcode() : null)
                .status(status.toString())
                .jwtId(jwt != null ? jwt.getId() : null)
                .logInAttempt(logInAttempt)
                .passwordVerificationCode(passwordVerificationCode != null ? passwordVerificationCode : null)
                .build();
    }
}
