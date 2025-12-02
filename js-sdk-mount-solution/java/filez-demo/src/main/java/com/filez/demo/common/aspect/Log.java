package com.filez.demo.common.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that need logging
 */
@Target(ElementType.METHOD) // Method level usage
@Retention(RetentionPolicy.RUNTIME) // Runtime effective
@Documented
public @interface Log {
    String value() default ""; // Can be used to record operation description
}
