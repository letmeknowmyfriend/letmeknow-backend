package com.letmeknow.form;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class MemberAddressUpdateForm {
    @NotBlank(message = "도시는 필수입니다.")
    private String city;

    @NotBlank(message = "도로명은 필수입니다.")
    private String street;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;

    @Builder
    protected MemberAddressUpdateForm(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
