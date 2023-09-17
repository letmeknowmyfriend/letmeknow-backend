package com.letmeknow.exception.store;

public class DuplicateStoreNameException extends RuntimeException {
    public DuplicateStoreNameException(String message) {
        super(message);
    }
}
