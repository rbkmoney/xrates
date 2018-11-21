package com.rbkmoney.xrates.exception;

public class UnknownSourceException extends RuntimeException {

    public UnknownSourceException() {
    }

    public UnknownSourceException(String message) {
        super(message);
    }

    public UnknownSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownSourceException(Throwable cause) {
        super(cause);
    }

    public UnknownSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
