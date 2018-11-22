package com.rbkmoney.xrates.exchange;

import com.rbkmoney.xrates.domain.SourceData;
import com.rbkmoney.xrates.domain.SourceType;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExchangeTest {

    @Test
    public void testCheckBoundsInSourceDataWithInitialTime() {
        Instant initialTime = Instant.parse("2018-11-21T21:00:00.000Z");
        int delay = 500;

        Source source = new Source(
                time -> Collections.emptyList(),
                new CronResolver("00 00 * * *", "Europe/Moscow", 500),
                initialTime,
                SourceType.CBR
        );

        SourceData sourceData = source.getSourceDataFromInitialTime();
        assertEquals(sourceData.getNextExecutionTime().plusMillis(delay), sourceData.getUpperBound());
        assertEquals(initialTime, sourceData.getLowerBound());
        assertTrue(sourceData.getRates().isEmpty());
    }

}
