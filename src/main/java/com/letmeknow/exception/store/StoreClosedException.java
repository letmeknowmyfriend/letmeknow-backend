package com.letmeknow.exception.store;

public class StoreClosedException extends RuntimeException {
    private Long storeId;
    public StoreClosedException(String message, Long storeId) {
        super(message);
        this.storeId = storeId;
    }
}
