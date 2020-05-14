package com.rbkmoney.xrates.exchange.impl.provider.psb.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PsbPaymentSystem {

    MASTERCARD("MasterCard"),
    VISA("Visa");

    private final String value;

}
