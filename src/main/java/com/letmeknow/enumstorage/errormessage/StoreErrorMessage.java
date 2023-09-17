package com.letmeknow.enumstorage.errormessage;

import lombok.Getter;

@Getter
public enum StoreErrorMessage {
    DUPLICATE_STORE_NAME("이미 존재하는 가게입니다."),
    IS_NOT_MEMBERS_STORE("회원이 가지고 있지 않은 가게입니다."),
    NO_SUCH_STORE("해당하는 가게가 없습니다."),
    DUPLICATE_ITEM_NAME("가게에 이미 같은 이름의 상품이 있습니다."),
    STORE_CLOSED("가게가 닫혀있습니다.");

    private final String message;

    StoreErrorMessage(String message) {
        this.message = message;
    }
}
