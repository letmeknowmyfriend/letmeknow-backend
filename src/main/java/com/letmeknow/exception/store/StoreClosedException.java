package com.letmeknow.exception.store;

public class StoreClosedException extends RuntimeException {
    private long storeId;
    public StoreClosedException(String message, long storeId) {
        super(message);
        this.storeId = storeId;
    }
}
