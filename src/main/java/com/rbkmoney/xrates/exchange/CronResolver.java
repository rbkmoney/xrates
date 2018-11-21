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

@Getter
public class CronResolver {

    public static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

    public static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    private final Cron cron;

    private final ZoneId timezone;

    private final ExecutionTime executionTime;

    private final Duration delay;

    public CronResolver(String cron, String timezone, int delayMs) {
        this(CRON_PARSER.parse(cron), ZoneId.of(timezone), Duration.ofMillis(delayMs));
    }

    public CronResolver(Cron cron, ZoneId timezone, Duration delay) {
        this.cron = cron;
        this.timezone = timezone;
        this.delay = delay;
        this.executionTime = ExecutionTime.forCron(cron);
    }

    public Instant getNextExecution(Instant time) {
        return executionTime.nextExecution(time.atZone(timezone))
                .map(zonedTime -> zonedTime.toInstant())
                .orElse(time);
    }

    public Instant getNextExecutionWithDelay(Instant time) {
        return executionTime.nextExecution(time.atZone(timezone))
                .map(zonedTime -> zonedTime.plus(delay).toInstant())
                .orElseThrow(IllegalStateException::new);
    }

}
