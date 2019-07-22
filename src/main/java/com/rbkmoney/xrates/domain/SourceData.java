package com.rbkmoney.xrates.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class SourceData {

    private final Instant lowerBound;
    private final Instant upperBound;
    private final Instant nextExecutionTime;
    private final List<ExchangeRate> rates;

}
