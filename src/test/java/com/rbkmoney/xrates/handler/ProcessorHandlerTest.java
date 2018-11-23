package com.rbkmoney.xrates.handler;

import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.msgpack.Nil;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import com.rbkmoney.woody.api.flow.error.WUnavailableResultException;
import com.rbkmoney.woody.api.flow.error.WUndefinedResultException;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.service.ExchangeRateService;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"sources.needInitialize=false"})
public class ProcessorHandlerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private ExchangeRateService exchangeRateService;

    private ProcessorSrv.Iface client;

    @Before
    public void setup() throws URISyntaxException {
        client = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/v1/processor"))
                .build(ProcessorSrv.Iface.class);
    }

    @Test(expected = WUndefinedResultException.class)
    public void testWhenInvalidSource() throws TException {
        client.processSignal(
                new SignalArgs(
                        Signal.init(
                                new InitSignal(Value.bin(Geck.toMsgPack(Value.nl(new Nil()))))
                        ),
                        new Machine(
                                "rates",
                                "incorrect",
                                Collections.emptyList(),
                                new HistoryRange()
                        )
                ));
    }

    @Test(expected = WUnavailableResultException.class)
    public void testWhenProviderThrowError() throws TException {
        given(exchangeRateService.getExchangeRatesBySourceType(any()))
                .willThrow(new ProviderUnavailableResultException("test"));

        client.processSignal(
                new SignalArgs(
                        Signal.init(
                                new InitSignal(Value.bin(Geck.toMsgPack(Value.nl(new Nil()))))
                        ),
                        new Machine(
                                "rates",
                                "CBR",
                                Collections.emptyList(),
                                new HistoryRange()
                        )
                ));
    }

}
