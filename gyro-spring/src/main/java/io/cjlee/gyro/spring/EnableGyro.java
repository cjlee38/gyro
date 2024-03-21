package io.cjlee.gyro.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ConfigurationPropertiesScan("io.cjlee.gyro.spring")
public @interface EnableGyro {
}
