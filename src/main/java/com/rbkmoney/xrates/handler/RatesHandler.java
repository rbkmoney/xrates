package com.rbkmoney.xrates.handler;

import com.rbkmoney.xrates.base.Rational;
import com.rbkmoney.xrates.exception.CurrencyNotFoundException;
import com.rbkmoney.xrates.exception.QuoteNotFoundException;
import com.rbkmoney.xrates.rate.*;
import com.rbkmoney.xrates.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.fraction.BigFraction;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatesHandler implements RatesSrv.Iface {

    private final ExchangeRateService exchangeRateService;

    @Override
    public ExchangeRateData getExchangeRates(String sourceId, String datetime) throws QuoteNotFound {
        try {
            Change change = exchangeRateService.getChangeByTime(sourceId, Instant.parse(datetime));
            return change.getCreated().getExchangeRateData();
        } catch (QuoteNotFoundException ex) {
            throw new QuoteNotFound();
        }
    }

    @Override
    public Rational getConvertedAmount(String sourceId, ConversionRequest conversionRequest)
            throws QuoteNotFound, CurrencyNotFound {
        try {
            BigFraction convertedAmount = exchangeRateService.getConvertedAmount(sourceId, conversionRequest);
            return new Rational(convertedAmount.getNumeratorAsLong(), convertedAmount.getDenominatorAsLong());
        } catch (QuoteNotFoundException ex) {
            throw new QuoteNotFound();
        } catch (CurrencyNotFoundException ex) {
            throw new CurrencyNotFound();
        }
    }
}
