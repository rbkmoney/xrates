package com.rbkmoney.xrates.util;

import org.apache.commons.math3.fraction.BigFraction;
import org.joda.money.CurrencyUnit;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class MoneyUtilTest {

    @Test
    public void testExchangeRateToRationalValue() {
        testRateRationalConversion(CurrencyUnit.JPY, CurrencyUnit.USD, new BigDecimal("33.333333333"), 333);
        testRateRationalConversion(CurrencyUnit.USD, CurrencyUnit.JPY, new BigDecimal("78.3123"), 666);
        testRateRationalConversion(CurrencyUnit.of("CLF"), CurrencyUnit.JPY, new BigDecimal("78.3123"), 234);
        testRateRationalConversion(CurrencyUnit.JPY, CurrencyUnit.of("CLF"), new BigDecimal("78.3123"), 234);


    }

    private void testRateRationalConversion(CurrencyUnit source, CurrencyUnit destination, BigDecimal rate, int sourceCount) {
        BigFraction rationalMinorRate = MoneyUtil.exchangeRateToRationalValue(source, destination, rate);
        assertEquals(rate.multiply(BigDecimal.valueOf(sourceCount)).stripTrailingZeros(),
                new BigFraction(sourceCount)
                        .multiply(rationalMinorRate)
                        .bigDecimalValue()
                        .movePointRight(source.getDecimalPlaces())
                        .movePointLeft(destination.getDecimalPlaces())
        );
    }

}
