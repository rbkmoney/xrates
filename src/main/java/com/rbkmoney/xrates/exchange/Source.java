package com.rbkmoney.xrates.exchange;

import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class Source {

    private final ExchangeProvider exchangeProvider;

    private final CronResolver cronResolver;

    private final Instant initialTime;

    private final SourceType sourceType;

    public SourceData getSourceDataFromInitialTime() throws ProviderUnavailableResultException {
        return getSourceData(initialTime);
    }

    public SourceData getSourceData(Instant executionTime) throws ProviderUnavailableResultException {
        Instant lowerBound = executionTime;
        Instant upperBound = cronResolver.getNextExecutionWithDelay(executionTime);

        List<ExchangeRate> rates = exchangeProvider.getExchangeRates(lowerBound);

        return new SourceData(
                lowerBound,
                upperBound,
                cronResolver.getNextExecution(executionTime),
                rates
        );
    }

}
