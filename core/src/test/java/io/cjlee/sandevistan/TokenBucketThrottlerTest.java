package io.cjlee.sandevistan;

import static org.assertj.core.api.Assertions.*;

import io.cjlee.sandevistan.support.IntervaledLatch;
import io.cjlee.sandevistan.support.TestUtils;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TokenBucketThrottlerTest {
    private static final Logger logger = LoggerFactory.getLogger(TokenBucketThrottler.class);

    @Test
    void withoutDelay() throws InterruptedException {
        int count = 3;
        Throttler throttler = new TokenBucketThrottler(count, 1, Duration.ofSeconds(1));
        IntervaledLatch latch = new IntervaledLatch(Duration.ZERO, count);
        Runnable runnable = () -> {
            latch.lap();
            logger.info("running");
        };

        TestUtils.repeat(count, () -> throttler.submit(runnable));

        latch.intervaled();
        Thread.sleep(1000L);

    }
}
