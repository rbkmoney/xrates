package com.rbkmoney.xrates.configuration;

import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinarium.client.TBaseAutomatonClient;
import com.rbkmoney.machinegun.stateproc.AutomatonSrv;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import com.rbkmoney.xrates.domain.SourceType;
import com.rbkmoney.xrates.exception.ProviderUnavailableResultException;
import com.rbkmoney.xrates.exchange.CronResolver;
import com.rbkmoney.xrates.exchange.Source;
import com.rbkmoney.xrates.exchange.impl.provider.cbr.CbrExchangeProvider;
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
            @Value("${sources.cbr.cron.timezone}") String timezone,
            @Value("${sources.cbr.cron.delayMs}") int delay,
            @Value("${sources.cbr.initialTime}") String initialTime
    ) {
        CronResolver cronResolver = new CronResolver(cron, timezone, delay);
        return new Source(cbrExchangeProvider, cronResolver, Instant.parse(initialTime), SourceType.CBR);
    }

    @Bean
    public AutomatonSrv.Iface automationThriftClient(
            @Value("${service.mg.url}") Resource resource,
            @Value("${service.mg.networkTimeout}") int networkTimeout
            ) throws IOException {
        return new THSpawnClientBuilder()
                .withAddress(resource.getURI())
                .withNetworkTimeout(networkTimeout)
                .build(AutomatonSrv.Iface.class);
    }

    @Bean
    public AutomatonClient<com.rbkmoney.machinegun.msgpack.Value, Change> automatonClient(
            @Value("${service.mg.namespace}") String namespace,
            AutomatonSrv.Iface automationThriftClient
    ) {
        return new TBaseAutomatonClient<>(automationThriftClient, namespace, Change.class);
    }

}
