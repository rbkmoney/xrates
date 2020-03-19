package com.rbkmoney.xrates.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentSystem {

    UNKNOWN(-1),
    VISA(0),
    MASTERCARD(1);

    private final int code;

    public static PaymentSystem findByName(String name) {
        for (PaymentSystem paymentSystem : values()) {
            if (paymentSystem.name().equalsIgnoreCase(name)) {
                return paymentSystem;
            }
        }
        return UNKNOWN;
    }

}
