package com.letmeknow.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.letmeknow.domain.Address;
import com.letmeknow.entity.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemporaryMember extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Address address;

    @NotBlank
    private String verificationCode;

    @Builder
    protected TemporaryMember(String name, String email, String password, String city, String street, String zipcode, String verificationCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = Address.builder()
                        .city(city)
                        .street(street)
                        .zipcode(zipcode)
                .build();
        this.verificationCode = verificationCode;
    }

    //== 비즈니스 로직 ==//
    public void updateVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
