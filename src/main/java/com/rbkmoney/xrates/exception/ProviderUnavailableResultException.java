package com.rbkmoney.xrates.exception;

public class ProviderUnavailableResultException extends RuntimeException {

    public ProviderUnavailableResultException() {
    }

    public ProviderUnavailableResultException(String message) {
        super(message);
    }

    public ProviderUnavailableResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderUnavailableResultException(Throwable cause) {
        super(cause);
    }

    public ProviderUnavailableResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
