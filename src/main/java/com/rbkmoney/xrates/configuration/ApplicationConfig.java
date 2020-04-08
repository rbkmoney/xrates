package com.rbkmoney.xrates.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.client.EventSinkClient;
import com.rbkmoney.machinarium.client.TBaseAutomatonClient;
import com.rbkmoney.machinarium.client.TBaseEventSinkClient;
import com.rbkmoney.machinegun.stateproc.AutomatonSrv;
import com.rbkmoney.machinegun.stateproc.EventSinkSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.CronResolver;
import com.rbkmoney.xrates.exchange.Source;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.CbrExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.PsbExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.data.PsbPaymentSystem;
import com.rbkmoney.xrates.rate.Change;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(
                new DefaultResponseErrorHandler() {

                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        try {
                            super.handleError(response);
                        } catch (NestedRuntimeException ex) {
                            throw new ProviderUnavailableResultException(ex);
                        }
                    }
                }
        );
        return restTemplate;
    }

    @Bean
    public CbrExchangeProvider cbrExchangeProvider(
            @Value("${sources.cbr.provider.url}") String url,
            @Value("${sources.cbr.provider.timezone}") ZoneId timezone,
            RestTemplate restTemplate
    ) {
        return new CbrExchangeProvider(url, timezone, restTemplate);
    }

    @Bean
    public Source cbrSource(
            CbrExchangeProvider cbrExchangeProvider,
            @Value("${sources.cbr.sourceId}") String sourceId,
            @Value("${sources.cbr.cron.value}") String cron,
            @Value("${sources.cbr.cron.timezone}") ZoneId timezone,
            @Value("${sources.cbr.cron.delay}") Duration delay,
            @Value("${sources.cbr.initialTime}") Instant initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(cbrExchangeProvider, cronResolver, initialTime, sourceId);
    }

    @Bean
    public PsbExchangeProvider psbMastercardExchangeProvider(
            @Value("${sources.psb-mastercard.provider.url}") String url,
            @Value("${sources.psb-mastercard.provider.timezone}") ZoneId timezone,
            @Value("${sources.psb-mastercard.provider.terminalId}") String terminalId,
            @Value("${sources.psb-mastercard.provider.secretKey}") String secretKey,
            @Value("${sources.psb-mastercard.provider.secretKey}") PsbPaymentSystem paymentSystem,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        return new PsbExchangeProvider(url, timezone, terminalId, secretKey, paymentSystem, restTemplate, objectMapper);
    }

    @Bean
    public Source psbMastercardSource(
            PsbExchangeProvider psbMastercardExchangeProvider,
            @Value("${sources.psb-mastercard.sourceId}") String sourceId,
            @Value("${sources.psb-mastercard.cron.value}") String cron,
            @Value("${sources.psb-mastercard.cron.timezone}") ZoneId timezone,
            @Value("${sources.psb-mastercard.cron.delay}") Duration delay,
            @Value("${sources.psb-mastercard.initialTime}") Instant initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(psbMastercardExchangeProvider, cronResolver, initialTime, sourceId);
    }

    @Bean
    public PsbExchangeProvider psbVisaExchangeProvider(
            @Value("${sources.psb-visa.provider.url}") String url,
            @Value("${sources.psb-visa.provider.timezone}") ZoneId timezone,
            @Value("${sources.psb-visa.provider.terminalId}") String terminalId,
            @Value("${sources.psb-visa.provider.secretKey}") String secretKey,
            @Value("${sources.psb-visa.provider.secretKey}") PsbPaymentSystem paymentSystem,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        return new PsbExchangeProvider(url, timezone, terminalId, secretKey, paymentSystem, restTemplate, objectMapper);
    }

    @Bean
    public Source psbVisaSource(
            PsbExchangeProvider psbVisaExchangeProvider,
            @Value("${sources.psb-visa.sourceId}") String sourceId,
            @Value("${sources.psb-visa.cron.value}") String cron,
            @Value("${sources.psb-visa.cron.timezone}") ZoneId timezone,
            @Value("${sources.psb-visa.cron.delay}") Duration delay,
            @Value("${sources.psb-visa.initialTime}") Instant initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(psbVisaExchangeProvider, cronResolver, initialTime, sourceId);
    }

    @Bean
    public AutomatonSrv.Iface automationThriftClient(
            @Value("${service.mg.automaton.url}") Resource resource,
            @Value("${service.mg.networkTimeout}") int networkTimeout
    ) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(networkTimeout)
                .build(AutomatonSrv.Iface.class);
    }

    @Bean
    public AutomatonClient<com.rbkmoney.machinegun.msgpack.Value, Change> automatonClient(
            @Value("${service.mg.automaton.namespace}") String namespace,
            AutomatonSrv.Iface automationThriftClient
    ) {
        return new TBaseAutomatonClient<>(automationThriftClient, namespace, Change.class);
    }

    @Bean
    public EventSinkSrv.Iface eventSinkThriftClient(
            @Value("${service.mg.eventSink.url}") Resource resource,
            @Value("${service.mg.networkTimeout}") int networkTimeout
    ) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(networkTimeout)
                .build(EventSinkSrv.Iface.class);
    }

    @Bean
    public EventSinkClient<Change> eventSinkClient(
            @Value("${service.mg.eventSink.sinkId}") String eventSinkId,
            EventSinkSrv.Iface eventSinkThriftClient
    ) {
        return new TBaseEventSinkClient<>(eventSinkThriftClient, eventSinkId, Change.class);
    }

}
