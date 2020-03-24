package com.rbkmoney.xrates.exchange.impl.provider.psb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.domain.PaymentSystem;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.ExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.data.PsbExchangeRootData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.joda.money.CurrencyUnit;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PsbExchangeProvider implements ExchangeProvider {

    public static final String DEFAULT_ENDPOINT = "https://3ds.payment.ru/cgi-bin/curr_rate_by_date";

    public static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("Europe/Moscow");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final CurrencyUnit DESTINATION_CURRENCY_UNIT = CurrencyUnit.of("RUB");
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private final String url;

    private final ZoneId timezone;

    private final String terminalId;

    private final String secretKey;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public PsbExchangeProvider(String terminalId, String secretKey, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this(DEFAULT_ENDPOINT, terminalId, secretKey, restTemplate, objectMapper);
    }

    public PsbExchangeProvider(String url, String terminalId, String secretKey, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this(url, DEFAULT_TIMEZONE, terminalId, secretKey, restTemplate, objectMapper);
    }

    public PsbExchangeProvider(String url, ZoneId timezone, String terminalId, String secretKey, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.url = url;
        this.timezone = timezone;
        this.terminalId = terminalId;
        this.secretKey = secretKey;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExchangeRate> getExchangeRates(Instant time) throws ProviderUnavailableResultException {
        log.info("Trying to get exchange rates from psb endpoint, url='{}', time='{}'", url, time);
        LocalDate date = time.atZone(timezone).toLocalDate();

        PsbExchangeRootData psbExchangeRootData = request(buildUrl(url, terminalId, secretKey, date));
        validateResponse(psbExchangeRootData);

        List<ExchangeRate> exchangeRates = psbExchangeRootData.getRates().stream()
                .map(currency -> new ExchangeRate(
                        CurrencyUnit.of(currency.getCurrencyCode()),
                        DESTINATION_CURRENCY_UNIT,
                        currency.getValue(),
                        PaymentSystem.findByName(currency.getIps())
                )).collect(Collectors.toList());

        log.info("Exchange rates from psb have been retrieved, url='{}', time='{}', exchangeRates='{}'", url, time, exchangeRates);
        return exchangeRates;
    }

    private PsbExchangeRootData request(String url) throws ProviderUnavailableResultException {
        try {
            return objectMapper.readValue(restTemplate.getForObject(url, String.class), PsbExchangeRootData.class);
        } catch (IOException | NestedRuntimeException ex) {
            throw new ProviderUnavailableResultException(String.format("Failed to get data from psb endpoint, url='%s'", url), ex);
        }
    }

    private String buildUrl(String endpoint, String terminalId, String secretKey, LocalDate date) {
        return UriComponentsBuilder
                .fromUriString(endpoint)
                .queryParam("TERMINAL", terminalId)
                .queryParam("DATE", date.format(DATE_TIME_FORMATTER))
                .queryParam("P_SIGN", buildSign(terminalId, date.format(DATE_TIME_FORMATTER), secretKey))
                .build()
                .toUriString();
    }

    private String buildSign(String terminalId, String date, String secretKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Hex.decodeHex(secretKey), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(keySpec);

            return Hex.encodeHexString(
                    mac.doFinal(prepareDataForSign(terminalId, date).getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException | DecoderException ex) {
            throw new ProviderUnavailableResultException(ex);
        }
    }

    private String prepareDataForSign(String... parameters) {
        StringBuilder sb = new StringBuilder();
        for (String parameter : parameters) {
            sb.append(parameter.length());
            sb.append(parameter);
        }
        return sb.toString();
    }

    private void validateResponse(PsbExchangeRootData psbExchangeRootData) throws ProviderUnavailableResultException {
        if (psbExchangeRootData.hasError()) {
            throw new ProviderUnavailableResultException(String.format("Error in psb response, error='%s'", psbExchangeRootData.getError()));
        }
        if (psbExchangeRootData.getRates() == null || psbExchangeRootData.getRates().isEmpty()) {
            throw new ProviderUnavailableResultException("Empty currency list in psb response");
        }
    }
}
