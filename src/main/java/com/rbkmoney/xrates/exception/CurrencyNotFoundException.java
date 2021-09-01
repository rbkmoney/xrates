package com.rbkmoney.xrates.exception;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException() {
    }

    public CurrencyNotFoundException(String message) {
        super(message);
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurrencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public CurrencyNotFoundException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
