package com.yhzdys.litchi.support.spring;

import com.yhzdys.litchi.annotation.MultiTransactional;
import com.yhzdys.litchi.transaction.Transaction;
import com.yhzdys.litchi.transaction.TxExecutor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * 事务代理对象
 */
class TxInterceptor implements MethodInterceptor {

    private final TxExecutor executor = TxExecutor.INSTANCE;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MultiTransactional annotation = invocation.getMethod().getAnnotation(MultiTransactional.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(invocation.getMethod(), MultiTransactional.class);
        }
        if (annotation == null) {
            return invocation.proceed();
        }
        return executor.execute(new Transaction(invocation, annotation));
    }
}
