package io.cjlee.gyro.marker;

import io.cjlee.gyro.Throttler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

/**
 * Denotes a {@link Throttler} implementation as <b>bounded</b>. In bounded mode, there is a
 * hard limit on the number of tasks that can be queued for execution. If this limit is exceeded,
 * any additional tasks submitted via {@link Throttler#submit(Runnable)} or
 * {@link Throttler#submit(Callable)} will be rejected immediately.
 * <p>
 * This approach ensures resource constraints are respected, making it suitable for scenarios
 * where it is critical to avoid overloading the system or the underlying service. However,
 * users of bounded throttlers should be prepared to handle task rejection.
 *
 * @see // TODO
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Bounded {
}

