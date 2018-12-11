package com.rbkmoney.xrates.handler;

import com.rbkmoney.machinarium.client.EventSinkClient;
import com.rbkmoney.machinarium.domain.TSinkEvent;
import com.rbkmoney.xrates.rate.*;
import com.rbkmoney.xrates.util.ProtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventSinkHandler implements EventSinkSrv.Iface {

    private final EventSinkClient<Change> eventSinkClient;

    public EventSinkHandler(EventSinkClient<Change> eventSinkClient) {
        this.eventSinkClient = eventSinkClient;
    }

    @Override
    public List<SinkEvent> getEvents(EventRange eventRange) throws TException {
        List<TSinkEvent<Change>> events;
        if (eventRange.isSetAfter()) {
            events = eventSinkClient.getEvents(eventRange.getLimit(), eventRange.getAfter());
        } else {
            events = eventSinkClient.getEvents(eventRange.getLimit());
        }

        return events.stream()
                .map(ProtoUtil::toSinkEvent)
                .collect(Collectors.toList());
    }

    @Override
    public long getLastEventID() throws NoLastEvent, TException {
        return eventSinkClient.getLastEventId().orElse(Long.MIN_VALUE);
    }

}
