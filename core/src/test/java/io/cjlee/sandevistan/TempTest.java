package io.cjlee.sandevistan;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempTest {
    private static final Logger logger = LoggerFactory.getLogger(TempTest.class);

    @Test
    void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        Runnable command = () -> {
            logger.info("start");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
            logger.info("exit");
        };
        latch.await();
    }
}
