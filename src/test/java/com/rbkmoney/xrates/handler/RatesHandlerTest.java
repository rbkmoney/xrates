package com.rbkmoney.xrates.handler;

import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.xrates.exception.CurrencyNotFoundException;
import com.rbkmoney.xrates.exception.QuoteNotFoundException;
import com.rbkmoney.xrates.rate.ConversionRequest;
import com.rbkmoney.xrates.rate.CurrencyNotFound;
import com.rbkmoney.xrates.rate.QuoteNotFound;
import com.rbkmoney.xrates.rate.RatesSrv;
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
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@TestPropertySource(properties = {"sources.needInitialize=false"})
public class RatesHandlerTest {

    @LocalServerPort
    private int port;

    private RatesSrv.Iface client;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Before
    public void setup() throws URISyntaxException {
        client = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/v1/rates"))
                .build(RatesSrv.Iface.class);
    }

    @Test(expected = QuoteNotFound.class)
    public void testGetChangeByTimeWhenQuoteNotFound() throws TException {
        given(exchangeRateService.getChangeByTime(any(), any()))
                .willThrow(new QuoteNotFoundException());
        client.getExchangeRates("SOURCE", Instant.now().toString());
    }

    @Test(expected = QuoteNotFound.class)
    public void testGetConvertedAmountWhenQuoteNotFound() throws TException {
        given(exchangeRateService.getConvertedAmount(any(), any()))
                .willThrow(new QuoteNotFoundException());
        client.getConvertedAmount("SOURCE", new ConversionRequest("USD", "RUB", 1000));
    }

    @Test(expected = CurrencyNotFound.class)
    public void testGetConvertedAmountWhenCurrencyNotFound() throws TException {
        given(exchangeRateService.getConvertedAmount(any(), any()))
                .willThrow(new CurrencyNotFoundException());
        client.getConvertedAmount("SOURCE", new ConversionRequest("USD", "RUB", 1000));
    }

}
