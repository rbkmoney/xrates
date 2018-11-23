package com.rbkmoney.xrates.service;

import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.exception.MachineAlreadyExistsException;
import com.rbkmoney.machinegun.msgpack.Nil;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exception.UnknownSourceException;
import com.rbkmoney.xrates.exchange.Source;
import com.rbkmoney.xrates.rate.Change;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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
                    automatonClient.start(source.getSourceType().toString(), Value.nl(new Nil())); //nil?? ok, fine
                    log.info("Source '{}' have been initialized", source.getSourceType());
                } catch (MachineAlreadyExistsException ex) {
                    log.info("Source '{}' already exists", source.getSourceType());
                }
            }
        } catch (WRuntimeException ex) {
            log.error("Failed to init sources", ex);
            throw ex;
        }
    }

    public SourceData getExchangeRatesBySourceType(SourceType sourceType) throws ProviderUnavailableResultException, UnknownSourceException {
        log.info("Trying to get initial exchange rates, sourceType='{}'", sourceType);
        SourceData sourceData = getSourceByType(sourceType).getSourceDataFromInitialTime();
        log.info("Exchange rates have been retrieved, sourceType='{}', from='{}', to='{}', count='{}', nextExecutionTime='{}'",
                sourceType, sourceData.getLowerBound(), sourceData.getUpperBound(), sourceData.getRates().size(), sourceData.getNextExecutionTime());
        return sourceData;
    }

    public SourceData getExchangeRatesBySourceType(Instant time, SourceType sourceType) throws ProviderUnavailableResultException, UnknownSourceException {
        log.info("Trying to get exchange rates, time='{}', sourceType='{}'", time, sourceType);
        SourceData sourceData = getSourceByType(sourceType).getSourceData(time);
        log.info("Exchange rates have been retrieved, sourceType='{}', from='{}', to='{}', count='{}', nextExecutionTime='{}'",
                sourceType, sourceData.getLowerBound(), sourceData.getUpperBound(), sourceData.getRates().size(), sourceData.getNextExecutionTime());
        return sourceData;
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
