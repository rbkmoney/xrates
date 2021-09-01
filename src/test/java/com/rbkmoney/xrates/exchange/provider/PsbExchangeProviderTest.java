package com.rbkmoney.xrates.exchange.provider;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.ExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.CbrExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.PsbExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.data.PsbPaymentSystem;
import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class PsbExchangeProviderTest {

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper mapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    private MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();

    @Test
    public void testWithValidData() {
        String terminalId = "12345";
        String secretKey = "C50E41160302E0F5D6D59F1AA3925C45";
        LocalDate date = LocalDate.of(2020, 04, 11);

        mockServer.expect(
                requestTo(
                        PsbExchangeProvider.DEFAULT_ENDPOINT +
                        "?TERMINAL=" + terminalId +
                        "&DATE=" + PsbExchangeProvider.DATE_TIME_FORMATTER.format(date) +
                        "&P_SIGN=7734df96d9f918c2f374091509a8903b8654b46f"
                )
        ).andRespond(
                withSuccess(
                        "{'rates':[" +
                        "{'CURR':'USD','IPS':'MasterCard','BUY':'61.55','CB':'63.1385'}," +
                        "{'CURR':'USD','IPS':'Visa','BUY':'60.32','CB':'63.1385'}," +
                        "{'CURR':'EUR','IPS':'MasterCard','BUY':'68','CB':'69.5976'}," +
                        "{'CURR':'EUR','IPS':'Visa','BUY':'66.36','CB':'69.5976'}" +
                        "]}",
                        MediaType.TEXT_PLAIN
                )
        );

        ExchangeProvider exchangeProvider =
                new PsbExchangeProvider(terminalId, secretKey, PsbPaymentSystem.MASTERCARD, restTemplate, mapper);
        List<ExchangeRate> exchangeRates = exchangeProvider.getExchangeRates(
                date.atStartOfDay()
                        .atZone(CbrExchangeProvider.DEFAULT_TIMEZONE)
                        .toInstant()
        );
        assertEquals(2, exchangeRates.size());
        List<CurrencyUnit> expectedSourceCurrencies = List.of(CurrencyUnit.USD, CurrencyUnit.EUR);
        exchangeRates.forEach(exchangeRate -> {
            assertTrue(expectedSourceCurrencies.contains(exchangeRate.getSourceCurrency()));
            assertEquals(PsbExchangeProvider.DESTINATION_CURRENCY_UNIT, exchangeRate.getDestinationCurrency());
            assertNotNull(exchangeRate.getConversionRate());
        });

        mockServer.verify();
    }

    @Test(expected = ProviderUnavailableResultException.class)
    public void testWhenError() {
        mockServer
                .expect(anything())
                .andRespond(withSuccess(
                        "{'ERROR':'TERMINAL IS NULL'}",
                        MediaType.parseMediaType("text/plain; charset=UTF-8")
                ));

        ExchangeProvider exchangeProvider =
                new PsbExchangeProvider("terminalId", "secretKey", PsbPaymentSystem.MASTERCARD, restTemplate, mapper);
        exchangeProvider.getExchangeRates(Instant.now());
    }

    @Test(expected = ProviderUnavailableResultException.class)
    public void testWhenResultIsEmpty() {
        mockServer
                .expect(anything())
                .andRespond(withSuccess("{'rates':[]}", MediaType.parseMediaType("text/plain; charset=UTF-8")));

        ExchangeProvider exchangeProvider =
                new PsbExchangeProvider("terminalId", "secretKey", PsbPaymentSystem.MASTERCARD, restTemplate, mapper);
        exchangeProvider.getExchangeRates(Instant.now());
    }

}
