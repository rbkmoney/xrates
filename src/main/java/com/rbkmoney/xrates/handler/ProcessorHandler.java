package com.rbkmoney.xrates.handler;

import com.rbkmoney.machinarium.domain.CallResultData;
import com.rbkmoney.machinarium.domain.SignalResultData;
import com.rbkmoney.machinarium.domain.TMachine;
import com.rbkmoney.machinarium.domain.TMachineEvent;
import com.rbkmoney.machinarium.handler.AbstractProcessorHandler;
import com.rbkmoney.machinegun.msgpack.Nil;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.ComplexAction;
import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.rate.Change;
import com.rbkmoney.xrates.service.ExchangeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.rbkmoney.xrates.util.ProtoUtil.*;

@Slf4j
@Component
public class ProcessorHandler extends AbstractProcessorHandler<Value, Change> {

    private final ExchangeRateService exchangeRateService;

    public ProcessorHandler(ExchangeRateService exchangeRateService) {
        super(Value.class, Change.class);
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    protected SignalResultData<Change> processSignalInit(TMachine<Change> tMachine,
                                                         Value value) {
        String machineId = tMachine.getMachineId();
        log.info("Trying to process signal init, namespace='{}', machineId='{}'", tMachine.getNs(), machineId);

        SourceData sourceData = exchangeRateService.getExchangeRatesBySourceType(machineId);

        SignalResultData<Change> signalResultData = new SignalResultData<>(
                Value.nl(new Nil()),
                Collections.singletonList(buildCreatedChange(sourceData)),
                buildComplexActionWithDeadline(sourceData.getNextExecutionTime(), buildLastEventHistoryRange())
        );

        log.info("Response: {}", signalResultData);
        return signalResultData;
    }

    @Override
    protected SignalResultData<Change> processSignalTimeout(TMachine<Change> tMachine,
                                                            List<TMachineEvent<Change>> tMachineEvents) {
        String machineId = tMachine.getMachineId();

        log.info("Trying to process signal timeout, namespace='{}', machineId='{}', events='{}'",
                tMachine.getNs(), machineId, tMachineEvents);

        Change change = getLastEvent(tMachineEvents);
        if (change == null) {
            throw new IllegalStateException("Failed to process signal timeout because previous changes not found");
        }
        SourceData sourceData = exchangeRateService.getExchangeRatesBySourceType(getUpperBound(change), machineId);

        SignalResultData<Change> signalResultData = new SignalResultData<>(
                Value.nl(new Nil()),
                Collections.singletonList(buildCreatedChange(sourceData)),
                buildComplexActionWithDeadline(sourceData.getNextExecutionTime(), buildLastEventHistoryRange())
        );
        log.info("Response: {}", signalResultData);
        return signalResultData;
    }

    @Override
    protected CallResultData<Change> processCall(String namespace,
                                                 String machineId,
                                                 Value args,
                                                 List<TMachineEvent<Change>> tMachineEvents) {
        return new CallResultData<>(
                Value.nl(new Nil()),
                getLastEvent(tMachineEvents),
                Collections.emptyList(),
                new ComplexAction());
    }

}
