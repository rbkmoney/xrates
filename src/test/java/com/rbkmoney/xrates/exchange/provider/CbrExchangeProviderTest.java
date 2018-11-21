package com.rbkmoney.xrates.exchange.provider;

import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.ExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.CbrExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.adapter.CbrLocalDateXmlAdapter;
import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class CbrExchangeProviderTest {

    private RestTemplate restTemplate = new RestTemplate();

    private MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).build();

    @Test
    public void testWithValidData() {
        String currencyCode = "AUD";
        String valueString = "47.33523232";
        LocalDate date = LocalDate.now();

        mockServer.expect(
                requestTo(CbrExchangeProvider.DEFAULT_ENDPOINT + "?date_req=" + CbrExchangeProvider.DATE_TIME_FORMATTER.format(date))
        ).andRespond(
                withSuccess(
                        "<ValCurs Date=\"" + CbrLocalDateXmlAdapter.DATE_FORMATTER.format(date) + "\" name=\"Foreign Currency Market\">\n" +
                                "<Valute ID=\"R01010\">\n" +
                                "<NumCode>036</NumCode>\n" +
                                "<CharCode>" + currencyCode + "</CharCode>\n" +
                                "<Nominal>1</Nominal>\n" +
                                "<Name>Австралийский доллар</Name>\n" +
                                "<Value>" + valueString + "</Value>\n" +
                                "</Valute>" +
                                "</ValCurs>",
                        MediaType.parseMediaType("application/xml; charset=windows-1251")
                )
        );

        ExchangeProvider exchangeProvider = new CbrExchangeProvider(restTemplate);
        List<ExchangeRate> exchangeRates = exchangeProvider.getExchangeRates(
                date.atStartOfDay()
                        .atZone(CbrExchangeProvider.DEFAULT_TIMEZONE)
                        .toInstant()
        );
        assertEquals(1, exchangeRates.size());
        ExchangeRate exchangeRate = exchangeRates.get(0);
        assertEquals(CurrencyUnit.of(currencyCode), exchangeRate.getSourceCurrency());
        assertEquals(CbrExchangeProvider.DESTINATION_CURRENCY_UNIT, exchangeRate.getDestinationCurrency());
        assertEquals(new BigDecimal(valueString), exchangeRate.getConversionRate());

        mockServer.verify();
    }

    @Test(expected = ProviderUnavailableResultException.class)
    public void testWithInvalidDate() {
        mockServer.expect(requestTo(CbrExchangeProvider.DEFAULT_ENDPOINT + "?date_req=20/11/2018"))
                .andRespond(
                        withSuccess(
                                "<ValCurs Date=\"19.11.2018\" name=\"Foreign Currency Market\">\n" +
                                        "<Valute ID=\"R01010\">\n" +
                                        "<NumCode>036</NumCode>\n" +
                                        "<CharCode>AUD</CharCode>\n" +
                                        "<Nominal>1</Nominal>\n" +
                                        "<Name>Австралийский доллар</Name>\n" +
                                        "<Value>33.3333</Value>\n" +
                                        "</Valute>" +
                                        "</ValCurs>",
                                MediaType.parseMediaType("application/xml; charset=windows-1251")
                        )
                );
        try {
            ExchangeProvider exchangeProvider = new CbrExchangeProvider(restTemplate);
            exchangeProvider.getExchangeRates(
                    LocalDate.of(2018, 11, 20).atStartOfDay()
                            .atZone(CbrExchangeProvider.DEFAULT_TIMEZONE)
                            .toInstant()
            );
        } finally {
            mockServer.verify();
        }
    }

    @Test(expected = ProviderUnavailableResultException.class)
    public void testWhenReturnError() {
        LocalDate date = LocalDate.now();
        mockServer.expect(requestTo(buildExpectedUrl(date)))
                .andRespond(
                        withServerError()
                );

        try {
            ExchangeProvider exchangeProvider = new CbrExchangeProvider(restTemplate);
            exchangeProvider.getExchangeRates(
                    date.atStartOfDay()
                            .atZone(CbrExchangeProvider.DEFAULT_TIMEZONE)
                            .toInstant()
            );
        } finally {
            mockServer.verify();
        }
    }

    @Test(expected = ProviderUnavailableResultException.class)
    public void testWithEmptyData() {
        LocalDate date = LocalDate.now();
        mockServer.expect(requestTo(buildExpectedUrl(date)))
                .andRespond(
                        withSuccess()
                                .contentType(MediaType.parseMediaType("application/xml; charset=windows-1251"))
                                .body(
                                        "<ValCurs Date=\"" + CbrLocalDateXmlAdapter.DATE_FORMATTER.format(date) + "\" name=\"Foreign Currency Market\"></ValCurs>"
                                )
                );

        try {
            ExchangeProvider exchangeProvider = new CbrExchangeProvider(restTemplate);
            providerRequest(exchangeProvider, date);
        } finally {
            mockServer.verify();
        }
    }

    private String buildExpectedUrl(LocalDate date) {
        return CbrExchangeProvider.DEFAULT_ENDPOINT + "?date_req=" + CbrExchangeProvider.DATE_TIME_FORMATTER.format(date);
    }

    private List<ExchangeRate> providerRequest(ExchangeProvider exchangeProvider, LocalDate date) {
        return exchangeProvider.getExchangeRates(
                date.atStartOfDay()
                        .atZone(CbrExchangeProvider.DEFAULT_TIMEZONE)
                        .toInstant()
        );
    }

}
