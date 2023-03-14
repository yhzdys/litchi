package com.yhzdys.litchi.transaction.aspect;

import com.yhzdys.litchi.annotation.LitchiTransactional;
import com.yhzdys.litchi.transaction.Transaction;
import com.yhzdys.litchi.transaction.TransactionExecutor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 事务代理对象
 */
public class TransactionalInterceptor implements MethodInterceptor {

    private final TransactionExecutor executor = TransactionExecutor.INSTANCE;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        LitchiTransactional annotation = invocation.getMethod().getAnnotation(LitchiTransactional.class);
        Transaction transaction = new Transaction(invocation, annotation);
        return executor.execute(transaction);
    }
}
