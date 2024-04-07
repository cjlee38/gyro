package io.cjlee.gyro.marker;

import io.cjlee.gyro.Throttler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Indicates a {@link Throttler} implementation as <b>unbounded</b>. Unbounded throttlers do not
 * reject tasks based on a fixed limit. Instead, tasks submitted via {@link Throttler#submit(Runnable)}
 * or {@link Throttler#submit(Callable)} are always accepted, regardless of the current queue length.
 * <p>
 * While this mode ensures that no task submissions are rejected(except for {@link Throttler#shutdown(Duration)},
 * it potentially increases the risk of resource exhaustion under high load conditions. Users of unbounded throttlers
 * should consider the implications on system resources and stability.
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Unbounded {
}
