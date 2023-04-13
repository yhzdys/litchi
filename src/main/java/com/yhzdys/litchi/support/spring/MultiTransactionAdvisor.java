package com.yhzdys.litchi.support.spring;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.core.Ordered;

/**
 * spring aop切面定义
 */
public class MultiTransactionAdvisor extends AbstractPointcutAdvisor {

    private final Advice advice = new TxInterceptor();

    private final Pointcut pointcut = new TxPointcut();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }
}
