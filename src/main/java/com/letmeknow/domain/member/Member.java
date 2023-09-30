package com.letmeknow.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.letmeknow.domain.notification.Subscription;
import com.letmeknow.domain.notification.DeviceToken;
import lombok.*;
import com.letmeknow.domain.Address;
import com.letmeknow.domain.Store;
import com.letmeknow.domain.auth.Jwt;
import com.letmeknow.domain.auth.OAuth2;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.entity.BaseEntity;
import com.letmeknow.enumstorage.role.MemberRole;
import com.letmeknow.enumstorage.status.MemberStatus;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @NotNull
    @Embedded
    private Address address;

    @JsonIgnore
    @NotNull
    @OneToMany(mappedBy = "member")
    private List<Store> stores = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "member")
    private Set<Subscription> subscriptions = new HashSet<>();

    @NotNull
    @OneToMany(mappedBy = "member")
    private Set<DeviceToken> deviceTokens = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @NotNull
    @OneToMany(mappedBy = "member")
    private List<OAuth2> oAuth2s = new ArrayList<>();

    @NotNull
    @OneToMany(mappedBy = "member")
    private Set<Jwt> jwts = new HashSet<>();

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
        this.role = MemberRole.MEMBER;
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

    public void addJwt(Jwt jwt) {
        this.jwts.add(jwt);
    }

    public boolean removeJwt(Jwt jwt) {
        return this.jwts.remove(jwt);
    }

    public void addDeviceToken(DeviceToken deviceToken) {
        deviceTokens.add(deviceToken);
    }

    public void removeDeviceToken(DeviceToken deviceToken) {
        deviceTokens.remove(deviceToken);
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
                .jwtIds(jwts.stream().map(jwt -> jwt.getId()).collect(Collectors.toSet()))
                .logInAttempt(logInAttempt)
                .passwordVerificationCode(passwordVerificationCode != null ? passwordVerificationCode : null)
                .build();
    }
}
