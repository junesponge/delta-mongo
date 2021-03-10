package com.delta.annotation;

import java.lang.annotation.*;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/10 1:19 AM
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Collection {
    String value();
}
