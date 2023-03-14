package com.yhzdys.litchi.annotation;

import com.yhzdys.litchi.transaction.Propagation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LitchiTransactional {

    Class<? extends Throwable>[] rollbackFor() default {Exception.class};

    Class<? extends Throwable>[] noRollbackFor() default {};

    Propagation propagation() default Propagation.REQUIRED;
}
