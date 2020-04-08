package com.rbkmoney.xrates.service;

import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.exception.MachineAlreadyExistsException;
import com.rbkmoney.machinegun.msgpack.Nil;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.exception.CurrencyNotFoundException;
import com.rbkmoney.xrates.exception.QuoteNotFoundException;
import com.rbkmoney.xrates.exception.UnknownSourceException;
import com.rbkmoney.xrates.exchange.Source;
import com.rbkmoney.xrates.rate.Change;
import com.rbkmoney.xrates.rate.ConversionRequest;
import com.rbkmoney.xrates.rate.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.fraction.BigFraction;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.rbkmoney.xrates.util.ProtoUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final AutomatonClient<Value, Change> automatonClient;

    private final List<Source> sources;

    public void initSources() {
        try {
            for (Source source : sources) {
                try {
                    automatonClient.start(source.getSourceId(), Value.nl(new Nil())); //nil?? ok, fine
                    log.info("Source '{}' have been initialized", source.getSourceId());
                } catch (MachineAlreadyExistsException ex) {
                    log.info("Source '{}' already exists", source.getSourceId());
                }
            }
        } catch (WRuntimeException ex) {
            log.error("Failed to init sources", ex);
            throw ex;
        }
    }

    public SourceData getExchangeRatesBySourceType(String sourceId) {
        log.info("Trying to get initial exchange rates, sourceId='{}'", sourceId);
        SourceData sourceData = getSourceByType(sourceId).getSourceDataFromInitialTime();
        log.info("Exchange rates have been retrieved, sourceId='{}', from='{}', to='{}', count='{}', nextExecutionTime='{}'",
                sourceId, sourceData.getLowerBound(), sourceData.getUpperBound(), sourceData.getRates().size(), sourceData.getNextExecutionTime());
        return sourceData;
    }

    public SourceData getExchangeRatesBySourceType(Instant time, String sourceId) {
        log.info("Trying to get exchange rates, time='{}', sourceId='{}'", time, sourceId);
        SourceData sourceData = getSourceByType(sourceId).getSourceData(time);
        log.info("Exchange rates have been retrieved, sourceId='{}', from='{}', to='{}', count='{}', nextExecutionTime='{}'",
                sourceId, sourceData.getLowerBound(), sourceData.getUpperBound(), sourceData.getRates().size(), sourceData.getNextExecutionTime());
        return sourceData;
    }

    public BigFraction getConvertedAmount(String sourceId, ConversionRequest conversionRequest) {
        log.info("Trying to convert amount, sourceId='{}', conversionRequest='{}'", sourceId, conversionRequest);
        Change change = getChangeByTime(
                sourceId,
                Optional.ofNullable(conversionRequest.getDatetime())
                        .map(Instant::parse)
                        .orElse(Instant.now())
        );

        Quote quote = filterQuoteBySourceAndDestination(
                change.getCreated().getExchangeRateData().getQuotes(),
                conversionRequest.getSource(),
                conversionRequest.getDestination()
        );

        BigFraction convertedAmount = new BigFraction(conversionRequest.getAmount())
                .multiply(
                        new BigFraction(quote.getExchangeRate().getP(), quote.getExchangeRate().getQ())
                );
        log.info("Amount have been converted, sourceId='{}', conversionRequest='{}', convertedAmount='{}'", sourceId, conversionRequest, convertedAmount);
        return convertedAmount;
    }

    public Change getChangeByTime(String sourceId, Instant datetime) {
        log.info("Trying to get change by time, sourceId='{}', datetime='{}'", sourceId, datetime);
        Change firstChange = Optional.ofNullable(
                getFirstEvent(automatonClient.getEvents(sourceId, buildFirstEventHistoryRange()))
        ).orElseThrow(QuoteNotFoundException::new);

        Instant lowerBound = getLowerBound(firstChange);
        Duration totalDuration = Duration.between(lowerBound, datetime);
        if (totalDuration.isNegative()) {
            throw new QuoteNotFoundException();
        }

        long previousSequenceId = totalDuration.dividedBy(Duration.between(lowerBound, getUpperBound(firstChange)));
        Change change = Optional.ofNullable(
                getFirstEvent(automatonClient.getEvents(sourceId, buildFirstEventHistoryRangeAfter(previousSequenceId)))
        ).orElseThrow(QuoteNotFoundException::new);
        log.info("Change have been retrieved, sourceId='{}', datetime='{}', change='{}'", sourceId, datetime, change);
        return change;
    }

    private Source getSourceByType(String sourceId) {
        for (Source source : sources) {
            if (source.getSourceId().equals(sourceId)) {
                return source;
            }
        }
        throw new UnknownSourceException(String.format("Unknown source, sourceType='%s'", sourceId));
    }

    private Quote filterQuoteBySourceAndDestination(List<Quote> quotes, String sourceCurrency, String destinationCurrency) {
        return quotes.stream()
                .filter(
                        quote -> quote.getSource().getSymbolicCode().equals(sourceCurrency)
                                && quote.getDestination().getSymbolicCode().equals(destinationCurrency)
                )
                .findFirst()
                .orElseThrow(CurrencyNotFoundException::new);
    }

}
