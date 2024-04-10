package io.cjlee.gyro.support;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimestampLatch extends ThrottlerLatch {
    private static final Logger log = LoggerFactory.getLogger(TimestampLatch.class);

    private Duration toleration;

    public TimestampLatch(int expectCount, Duration toleration, VirtualTicker ticker) {
        super(expectCount, ticker);
        this.toleration = toleration;
    }

    public boolean isMatched(long... timestampMillis) {
        awaitLatch(Duration.ofMillis(10_000)); // TODO : What is the proper waiting time ?

        for (long timestampMilli : timestampMillis) {
            long timestampNano = TimeUnit.MILLISECONDS.toNanos(timestampMilli);
            boolean isMatched = false;
            for (Lap lap : laps()) {
                isMatched = lap.isMatched(timestampNano, toleration);
                if (isMatched) {
                    log.info("expected timestamp : {}ms ", timestampMilli + " and actual timestamp : " + lap);
                    break;
                }
            }
            if (!isMatched) {
                log.error("expected timestamp : {}ms ", timestampMilli + " but none of them matched");
                return false;
            }
        }
        return true;
    }
}
