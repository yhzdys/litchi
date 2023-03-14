package com.yhzdys.litchi.exception;

public class LitchiException extends RuntimeException {

    public LitchiException() {
    }

    public LitchiException(String message) {
        super(message);
    }

    public LitchiException(String message, Throwable cause) {
        super(message, cause);
    }

    public LitchiException(Throwable cause) {
        super(cause);
    }

    public LitchiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
