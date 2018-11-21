package com.rbkmoney.xrates.util;

import org.apache.commons.math3.fraction.BigFraction;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MoneyUtil {

    public static BigFraction exchangeRateToRationalValue(CurrencyUnit source, CurrencyUnit destination, BigDecimal conversionRate) {
        BigDecimal minorRate = conversionRate
                .movePointLeft(source.getDecimalPlaces())
                .movePointRight(destination.getDecimalPlaces());
        BigInteger denominator = minorRate.scale() > 0 ? BigInteger.TEN.pow(minorRate.scale()) : BigInteger.ONE;
        return new BigFraction(
                minorRate.remainder(BigDecimal.ONE)
                        .movePointRight(minorRate.scale()).toBigInteger()
                        .add(minorRate.toBigInteger().multiply(denominator)),
                denominator
        );
    }

}
