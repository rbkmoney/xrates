package com.rbkmoney.xrates.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;

@Getter
@ToString
@RequiredArgsConstructor
public class ExchangeRate {

    private final CurrencyUnit sourceCurrency;
    private final CurrencyUnit destinationCurrency;
    private final BigDecimal conversionRate;

}
