package com.rbkmoney.xrates.service;

import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.exception.MachineAlreadyExistsException;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.xrates.rate.Change;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@TestPropertySource(properties = {"sources.needInitialize=false"})
public class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @MockBean
    private AutomatonClient<Value, Change> automatonClient;

    @Test
    public void testInitSourcesWhenMachineAlreadyExists() {
        doThrow(MachineAlreadyExistsException.class)
                .when(automatonClient).start(any(), any());
        exchangeRateService.initSources();
    }

}
