package com.letmeknow.entity.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.letmeknow.auth.entity.DeviceToken;
import com.letmeknow.auth.entity.OAuth2;
import com.letmeknow.auth.entity.RefreshToken;
import com.letmeknow.dto.address.AddressDto;
import com.letmeknow.dto.member.MemberFindDto;
import com.letmeknow.entity.Address;
import com.letmeknow.entity.BaseEntity;
import com.letmeknow.entity.Store;
import com.letmeknow.entity.notification.Notification;
import com.letmeknow.entity.notification.Subscription;
import com.letmeknow.enumstorage.role.MemberRole;
import com.letmeknow.enumstorage.status.MemberStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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

    @NotBlank
    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    private String password;

    @Embedded
    @NotNull
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
    @OneToMany(mappedBy = "member")
    private Set<RefreshToken> refreshTokens = new HashSet<>();

    @NotNull
    @OneToMany(mappedBy = "member")
    private Set<Notification> notifications = new HashSet<>();

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
    private int logInAttempt = 0;

    @NotNull
    private Boolean consentToReceivePushNotification;

    private String passwordVerificationCode;
    private LocalDateTime passwordVerificationCodeExpiration;

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
        this.consentToReceivePushNotification = false;
    }

    @Builder
    protected Member(String email) {
        this.email = email;
        this.role = MemberRole.ADMIN;
        this.status = MemberStatus.ACTIVE;
    }

    //== 비즈니스 로직 ==//
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
        this.passwordVerificationCodeExpiration = LocalDateTime.now().plusMinutes(10);
    }

    public void deletePasswordVerificationCode() {
        this.passwordVerificationCode = null;
        this.passwordVerificationCodeExpiration = null;
    }

    public void deleteMember() {
        this.status = MemberStatus.DELETED;
    }

    public void consentToReceivePushNotification() {
        this.consentToReceivePushNotification = true;
    }

    public void refuseToReceivePushNotification() {
        this.consentToReceivePushNotification = false;
    }

    //==연관관계 메소드==//
    public void setStore(Store store) {
        stores.add(store);
    }

    public void addOAuth2(OAuth2 oAuth2) {
        oAuth2s.add(oAuth2);
    }

    public void addRefreshToken(RefreshToken refreshToken) {
        this.refreshTokens.add(refreshToken);
    }

    public boolean removeRefreshToken(RefreshToken refreshToken) {
        return this.refreshTokens.remove(refreshToken);
    }

    public void addDeviceToken(DeviceToken deviceToken) {
        deviceTokens.add(deviceToken);
    }

    //==DTO==//
    public MemberFindDto toMemberFindDto() {
        return MemberFindDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .address(AddressDto.builder()
                        .city(address.getCity())
                        .street(address.getStreet())
                        .zipcode(address.getZipcode())
                        .build())
                .consentToReceivePushNotification(consentToReceivePushNotification)
            .build();
    }
}
