package com.yhzdys.litchi.transaction.aspect;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

/**
 * spring aop切面定义
 */
public class LitchiTransactionAdvisor extends AbstractPointcutAdvisor {

    private final Advice advice = new TransactionalInterceptor();

    private final Pointcut pointcut = new TransactionPointcut();

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
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
