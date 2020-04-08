package com.rbkmoney.xrates.exchange;

import com.rbkmoney.xrates.domain.SourceData;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExchangeTest {

    @Test
    public void testCheckBoundsInSourceDataWithInitialTime() {
        Instant initialTime = Instant.parse("2016-12-31T21:00:00.000Z");
        Duration delay = Duration.ofHours(3);

        Source source = new Source(
                time -> Collections.emptyList(),
                new CronResolver("00 21 * * *", ZoneId.of("Europe/Moscow"), delay),
                initialTime,
                "SOURCE"
        );

        SourceData sourceData = source.getSourceDataFromInitialTime();
        assertEquals(sourceData.getNextExecutionTime().plus(delay), sourceData.getUpperBound());
        assertEquals(initialTime, sourceData.getLowerBound());
        assertTrue(sourceData.getRates().isEmpty());

        SourceData nextSourceData = source.getSourceData(sourceData.getUpperBound());
        assertEquals(sourceData.getUpperBound(), nextSourceData.getLowerBound());
        assertEquals(nextSourceData.getUpperBound(), nextSourceData.getNextExecutionTime().plus(delay));
        assertTrue(nextSourceData.getRates().isEmpty());
    }

    @Test
    public void testCheckBoundsInSourceDataWithInitialTimeAndInvertDelay() {
        Instant initialTime = Instant.parse("2020-02-17T21:00:00.000Z");
        Duration delay = Duration.ofHours(-1);

        Source source = new Source(
                time -> Collections.emptyList(),
                new CronResolver("00 01 * * *", ZoneId.of("Europe/Moscow"), delay),
                initialTime,
                "SOURCE"
        );

        SourceData sourceData = source.getSourceDataFromInitialTime();
        assertEquals(sourceData.getNextExecutionTime().plus(delay), sourceData.getUpperBound());
        assertEquals(initialTime, sourceData.getLowerBound());
        assertTrue(sourceData.getRates().isEmpty());

        SourceData nextSourceData = source.getSourceData(sourceData.getUpperBound());
        assertEquals(sourceData.getUpperBound(), nextSourceData.getLowerBound());
        assertEquals(nextSourceData.getUpperBound(), nextSourceData.getNextExecutionTime().plus(delay));
        assertTrue(nextSourceData.getRates().isEmpty());
    }

}
