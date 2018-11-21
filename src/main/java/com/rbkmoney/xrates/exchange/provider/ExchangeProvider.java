package com.rbkmoney.xrates.exchange.provider;

import com.rbkmoney.xrates.domain.ExchangeRate;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;

import java.time.Instant;
import java.util.List;

public interface ExchangeProvider {

    List<ExchangeRate> getExchangeRates(Instant time) throws ProviderUnavailableResultException;

}
