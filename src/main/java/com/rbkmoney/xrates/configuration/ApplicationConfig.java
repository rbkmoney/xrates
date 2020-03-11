package com.rbkmoney.xrates.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.client.EventSinkClient;
import com.rbkmoney.machinarium.client.TBaseAutomatonClient;
import com.rbkmoney.machinarium.client.TBaseEventSinkClient;
import com.rbkmoney.machinegun.stateproc.AutomatonSrv;
import com.rbkmoney.machinegun.stateproc.EventSinkSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.CronResolver;
import com.rbkmoney.xrates.exchange.Source;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.CbrExchangeProvider;
import com.rbkmoney.xrates.exchange.impl.provider.psb.PsbExchangeProvider;
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
            @Value("${sources.cbr.cron.value}") String cron,
            @Value("${sources.cbr.cron.timezone}") ZoneId timezone,
            @Value("${sources.cbr.cron.delay}") Duration delay,
            @Value("${sources.cbr.initialTime}") String initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(cbrExchangeProvider, cronResolver, Instant.parse(initialTime), SourceType.CBR);
    }

    @Bean
    public PsbExchangeProvider psbExchangeProvider(
            @Value("${sources.psb.provider.url}") String url,
            @Value("${sources.psb.provider.timezone}") ZoneId timezone,
            @Value("${sources.psb.provider.terminalId}") String terminalId,
            @Value("${sources.psb.provider.secretKey}") String secretKey,
            RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        return new PsbExchangeProvider(url, timezone, terminalId, secretKey,  restTemplate, objectMapper);
    }

    @Bean
    public Source psbSource(
            PsbExchangeProvider psbExchangeProvider,
            @Value("${sources.psb.cron.value}") String cron,
            @Value("${sources.psb.cron.timezone}") ZoneId timezone,
            @Value("${sources.psb.cron.delay}") Duration delay,
            @Value("${sources.psb.initialTime}") String initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(psbExchangeProvider, cronResolver, Instant.parse(initialTime), SourceType.PSB);
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
