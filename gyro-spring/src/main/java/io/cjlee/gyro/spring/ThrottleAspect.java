package io.cjlee.gyro.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ThrottleAspect {
    private final ThrottlerHandler handler;

    public ThrottleAspect(ThrottlerHandler handler) {
        this.handler = handler;
    }

    @Around("@annotation(io.cjlee.gyro.spring.Throttled)")
    public Object intercept(ProceedingJoinPoint joinPoint) {
        String group = getGroup(joinPoint);
        Runnable runnable = () -> {
            try {
                joinPoint.proceed(joinPoint.getArgs());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
        handler.handle(group, runnable);
        return null; // TODO : return future if possible
    }

    private String getGroup(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String annotatedGroup = signature.getMethod().getAnnotation(Throttled.class).group();
        if (annotatedGroup == null || annotatedGroup.isEmpty()) {
            throw new IllegalStateException("group must be specified");
        }
        return annotatedGroup;
    }
}
