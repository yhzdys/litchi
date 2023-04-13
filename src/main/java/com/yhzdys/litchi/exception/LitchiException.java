package com.yhzdys.litchi.exception;

public class LitchiException extends RuntimeException {

    public LitchiException(Throwable cause) {
        super(cause);
    }

    public LitchiException(String message) {
        super(message);
    }
}
