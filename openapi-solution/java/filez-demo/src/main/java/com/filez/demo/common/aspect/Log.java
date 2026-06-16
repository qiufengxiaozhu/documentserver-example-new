package com.filez.demo.common.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记需要记录日志的方法
 */
@Target(ElementType.METHOD) // 方法级别使用
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Documented
public @interface Log {
    String value() default ""; // 可以用来记录操作描述
}
