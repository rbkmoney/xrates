package com.rbkmoney.xrates.exchange.impl.provider.cbr;

import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.ExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.data.CbrExchangeRateData;
import lombok.extern.slf4j.Slf4j;
import org.joda.money.CurrencyUnit;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CbrExchangeProvider implements ExchangeProvider {

    public static final String DEFAULT_ENDPOINT = "https://www.cbr.ru/scripts/XML_daily.asp";

    public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Europe/Moscow");

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final CurrencyUnit DESTINATION_CURRENCY_UNIT = CurrencyUnit.of("RUB");

    private final String url;

    private final ZoneId timezone;

    private final RestTemplate restTemplate;

    public CbrExchangeProvider(RestTemplate restTemplate) {
        this(DEFAULT_ENDPOINT, restTemplate);
    }

    public CbrExchangeProvider(String url, RestTemplate restTemplate) {
        this(url, DEFAULT_TIMEZONE, restTemplate);
    }

    public CbrExchangeProvider(String url, ZoneId timezone, RestTemplate restTemplate) {
        this.url = url;
        this.timezone = timezone;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ExchangeRate> getExchangeRates(Instant time) {
        log.info("Trying to get exchange rates from cbr endpoint, url='{}', time='{}'", url, time);
        LocalDate date = time.atZone(timezone).toLocalDate();

        CbrExchangeRateData cbrExchangeRateData = request(buildUrl(url, date));
        validateResponse(cbrExchangeRateData);

        List<ExchangeRate> exchangeRates = cbrExchangeRateData.getCurrencies().stream()
                .map(
                        currency -> new ExchangeRate(
                                CurrencyUnit.of(currency.getCharCode()),
                                DESTINATION_CURRENCY_UNIT,
                                currency.getValue().divide(BigDecimal.valueOf(currency.getNominal()))
                        )
                ).collect(Collectors.toList());
        log.info("Exchange rates from cbr have been retrieved, url='{}', time='{}', exchangeRates='{}'", url, time, exchangeRates);
        return exchangeRates;
    }

    private CbrExchangeRateData request(String url) {
        try {
            return restTemplate.getForObject(url, CbrExchangeRateData.class);
        } catch (NestedRuntimeException ex) {
            throw new ProviderUnavailableResultException(String.format("Failed to get data from cbr endpoint, url='%s'", url), ex);
        }
    }

    private String buildUrl(String endpoint, LocalDate date) {
        return UriComponentsBuilder
                .fromUriString(endpoint)
                .queryParam("date_req", date.format(DATE_TIME_FORMATTER))
                .build()
                .toUriString();
    }

    private void validateResponse(CbrExchangeRateData cbrExchangeRateData) {
        if (cbrExchangeRateData.getCurrencies() == null || cbrExchangeRateData.getCurrencies().isEmpty()) {
            throw new ProviderUnavailableResultException("Empty currency list in cbr response");
        }
    }

}
