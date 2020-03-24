package com.rbkmoney.xrates.util;

import org.apache.commons.math3.fraction.BigFraction;
import org.joda.money.CurrencyUnit;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MoneyUtil {

    /**
     * Convert exchange rate to rational number, describing the exchange rate in minor units.
     * <p>
     * For example, we convert CLF to RUB at the exchange rate of 2640.4546 rubles.
     * The source of the currency is CLF with exponent 4, the destination currency is RUB with exponent 2.
     * The minor CLF unit will be equal to 0.26404546 of ruble, or 26.404546 of its minor unit.
     * As a result, in a rational representation, this will be equal to "26404546/1000000" or "13202273/500000".
     *
     * @param source       - source currency
     * @param destination  - destination currency
     * @param exchangeRate - rate at which source currency will be exchanged for destination currency
     * @return rational number
     */
    public static BigFraction exchangeRateToRationalValue(CurrencyUnit source, CurrencyUnit destination, BigDecimal exchangeRate) {
        BigDecimal minorRate = exchangeRate
                .movePointLeft(source.getDecimalPlaces() - destination.getDecimalPlaces());
        BigInteger denominator = minorRate.scale() > 0 ? BigInteger.TEN.pow(minorRate.scale()) : BigInteger.ONE;
        return new BigFraction(
                minorRate.remainder(BigDecimal.ONE)
                        .movePointRight(minorRate.scale()).toBigInteger()
                        .add(minorRate.toBigInteger().multiply(denominator)),
                denominator
        );
    }

}
