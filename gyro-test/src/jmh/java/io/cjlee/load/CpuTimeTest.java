package io.cjlee.load;

import com.google.common.util.concurrent.RateLimiter;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@SuppressWarnings("UnstableApiUsage")
@State(Scope.Benchmark)
public class CpuTimeTest {

    @Test
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    void rateLimiterTomcat() {
        // thread-base(tomcat),
        CpuTimeMeasurement measurement = new CpuTimeMeasurement();

        int requestCount = 10_000;
        int rps = 100;

        ExecutorService service = new ThreadPoolExecutor(10, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        RateLimiter rateLimiter = RateLimiter.create(rps);
        BigInteger result = measurement.measure(service, requestCount, () -> {
            while (!rateLimiter.tryAcquire()) {
                // polling
            }
        });

        boolean isBigger = result.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0;
        System.out.println("isBigger = " + isBigger);
        System.out.println("Long.MAX_VALUE = " + Long.MAX_VALUE);
        System.out.println("result = " + result);
    }
}
