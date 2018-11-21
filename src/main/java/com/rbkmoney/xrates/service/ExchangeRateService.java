package com.rbkmoney.xrates.service;

import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exception.UnknownSourceException;
import com.rbkmoney.xrates.exchange.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final List<Source> sources;

    public SourceData getExchangeRatesBySourceType(SourceType sourceType) throws ProviderUnavailableResultException, UnknownSourceException {
        return getSourceByType(sourceType).getSourceDataFromInitialTime();
    }

    public SourceData getExchangeRatesBySourceType(Instant time, SourceType sourceType) throws ProviderUnavailableResultException, UnknownSourceException {
        return getSourceByType(sourceType).getSourceData(time);
    }

    private Source getSourceByType(SourceType sourceType) {
        for (Source source : sources) {
            if (source.getSourceType() == sourceType) {
                return source;
            }
        }
        throw new UnknownSourceException(String.format("Unknown source, sourceType='%s'", sourceType));
    }

}
