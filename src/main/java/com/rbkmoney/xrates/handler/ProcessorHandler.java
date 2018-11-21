package com.rbkmoney.xrates.handler;

import com.rbkmoney.machinarium.domain.CallResultData;
import com.rbkmoney.machinarium.domain.SignalResultData;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.handler.AbstractProcessorHandler;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import com.rbkmoney.woody.api.flow.error.WUnavailableResultException;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.rate.Change;
import com.rbkmoney.xrates.rate.ExchangeRateData;
import com.rbkmoney.xrates.service.ExchangeRateService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.rbkmoney.xrates.util.ProtoUtil.*;

@Component
public class ProcessorHandler extends AbstractProcessorHandler<ExchangeRateData, Change> {

    private final ExchangeRateService exchangeRateService;

    public ProcessorHandler(ExchangeRateService exchangeRateService) {
        super(ExchangeRateData.class, Change.class);
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    protected SignalResultData<Change> processSignalInit(String namespace, String machineId, ExchangeRateData args) {
        SourceType sourceType = SourceType.valueOf(machineId);
        try {
            SourceData sourceData = exchangeRateService.getExchangeRatesBySourceType(sourceType);

            return new SignalResultData<>(
                    Collections.singletonList(buildCreatedChange(sourceData)),
                    buildComplexActionWithDeadline(sourceData.getNextExecutionTime(), buildLastEventHistoryRange())
            );
        } catch (ProviderUnavailableResultException ex) {
            throw new WUnavailableResultException(ex);
        }
    }

    @Override
    protected SignalResultData<Change> processSignalTimeout(String namespace, String machineId, List<TMachineEvent<Change>> tMachineEvents) {
        SourceType sourceType = SourceType.valueOf(machineId);
        Change change = getLastEvent(tMachineEvents);

        if (change == null) {
            throw new IllegalStateException("Failed to process signal timeout because previous changes not found");
        }

        Instant upperBound = Instant.parse(change.getCreated().getExchangeRateData().getInterval().getUpperBoundExclusive());
        try {
            SourceData sourceData = exchangeRateService.getExchangeRatesBySourceType(upperBound, sourceType);

            return new SignalResultData<>(
                    Collections.singletonList(buildCreatedChange(sourceData)),
                    buildComplexActionWithDeadline(sourceData.getNextExecutionTime(), buildLastEventHistoryRange())
            );
        } catch (ProviderUnavailableResultException ex) {
            throw new WUnavailableResultException(ex);
        }
    }

    @Override
    protected CallResultData<Change> processCall(String namespace, String machineId, ExchangeRateData args, List<TMachineEvent<Change>> tMachineEvents) {
        return new CallResultData<>(getLastEvent(tMachineEvents), Collections.emptyList(), new ComplexAction());
    }

    private Change getLastEvent(List<TMachineEvent<Change>> tMachineEvents) {
        if (!tMachineEvents.isEmpty()) {
            return tMachineEvents.get(tMachineEvents.size() - 1).getData();
        }
        return null;
    }

}
