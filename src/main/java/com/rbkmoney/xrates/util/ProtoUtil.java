package com.rbkmoney.xrates.util;

import com.rbkmoney.machinarium.domain.TSinkEvent;
import com.rbkmoney.machinegun.base.Timer;
import com.rbkmoney.machinegun.stateproc.*;
import com.rbkmoney.xrates.base.Rational;
import com.rbkmoney.xrates.base.TimestampInterval;
import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.rate.Event;
import com.rbkmoney.xrates.rate.SinkEvent;
import com.rbkmoney.xrates.rate.*;
import org.apache.commons.math3.fraction.BigFraction;
import org.joda.money.CurrencyUnit;

import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

public class ProtoUtil {

    public static ComplexAction buildComplexActionWithDeadline(Instant deadline, HistoryRange historyRange) {
        return buildComplexActionWithTimer(Timer.deadline(deadline.toString()), historyRange);
    }

    public static ComplexAction buildComplexActionWithTimer(Timer timer, HistoryRange historyRange) {
        SetTimerAction setTimerAction = new SetTimerAction();
        setTimerAction.setTimer(timer);
        setTimerAction.setRange(historyRange);

        ComplexAction complexAction = new ComplexAction();
        complexAction.setTimer(TimerAction.set_timer(setTimerAction));
        return complexAction;
    }

    public static HistoryRange buildLastEventHistoryRange() {
        HistoryRange historyRange = new HistoryRange();
        historyRange.setDirection(Direction.backward);
        historyRange.setLimit(1);
        return historyRange;
    }

    public static Change buildCreatedChange(SourceData sourceData) {
        ExchangeRateData exchangeRateData = new ExchangeRateData();
        exchangeRateData.setInterval(
                new TimestampInterval(sourceData.getLowerBound().toString(), sourceData.getUpperBound().toString())
        );
        exchangeRateData.setQuotes(
                sourceData.getRates().stream().map(
                        exchangeRate -> new Quote(
                                buildCurrency(exchangeRate.getSourceCurrency()),
                                buildCurrency(exchangeRate.getDestinationCurrency()),
                                buildExchangeRate(exchangeRate)
                        )
                ).collect(Collectors.toList())
        );
        return Change.created(new ExchangeRateCreated(exchangeRateData));
    }

    public static Rational buildExchangeRate(ExchangeRate exchangeRate) {
        BigFraction rationalValue = MoneyUtil.exchangeRateToRationalValue(
                exchangeRate.getSourceCurrency(),
                exchangeRate.getDestinationCurrency(),
                exchangeRate.getConversionRate()
        );

        return new Rational(rationalValue.getNumerator().longValueExact(), rationalValue.getDenominator().longValueExact());
    }

    public static Currency buildCurrency(CurrencyUnit currencyUnit) {
        return new Currency(currencyUnit.getCode(), (short) currencyUnit.getDecimalPlaces());
    }

    public static SinkEvent toSinkEvent(TSinkEvent<Change> changeTSinkEvent) {
        return new SinkEvent(
                changeTSinkEvent.getId(),
                changeTSinkEvent.getEvent().getCreatedAt().toString(),
                changeTSinkEvent.getSourceId(),
                new Event(Collections.singletonList(changeTSinkEvent.getEvent().getData())),
                changeTSinkEvent.getEvent().getId()
        );
    }

}
