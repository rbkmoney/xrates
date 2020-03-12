package com.rbkmoney.xrates.exchange;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.chrono.ChronoZonedDateTime;

@Getter
public class CronResolver {

    public static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

    public static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    private final Cron cron;

    private final ZoneId timezone;

    private final ExecutionTime executionTime;

    private final Duration delay;

    public CronResolver(String cron, ZoneId timezone, Duration delay) {
        this(CRON_PARSER.parse(cron), timezone, delay);
    }

    public CronResolver(Cron cron, ZoneId timezone, Duration delay) {
        this.cron = cron;
        this.timezone = timezone;
        this.delay = delay;
        this.executionTime = ExecutionTime.forCron(cron);
    }

    public Instant getNextExecution(Instant time) {
        return executionTime.nextExecution(time.atZone(timezone))
                .map(ChronoZonedDateTime::toInstant)
                .orElseThrow(IllegalStateException::new);
    }

    public Instant getNextExecutionWithDelay(Instant time) {
        return executionTime.nextExecution(time.atZone(timezone))
                .map(zonedTime -> zonedTime.plus(delay).toInstant())
                .orElseThrow(IllegalStateException::new);
    }

}
