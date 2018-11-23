package com.rbkmoney.xrates.listener;

import com.rbkmoney.xrates.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

    private final ExchangeRateService exchangeRateService;

    @Value("${sources.needInitialize}")
    private boolean needInitialize;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitialize) {
            log.info("Sources initialization...");
            exchangeRateService.initSources();
            log.info("Sources have been initialized");
        }
    }
}
