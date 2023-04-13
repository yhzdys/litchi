package com.yhzdys.litchi.transaction;

import com.yhzdys.litchi.annotation.MultiTransactional;
import org.aopalliance.intercept.MethodInvocation;

public class Transaction {

    private final MethodInvocation invocation;

    private final Class<? extends Throwable>[] rollbackFor;

    private final Class<? extends Throwable>[] noRollbackFor;

    private final Propagation propagation;

    public Transaction(MethodInvocation invocation, MultiTransactional annotation) {
        this.invocation = invocation;
        this.rollbackFor = annotation.rollbackFor();
        this.noRollbackFor = annotation.noRollbackFor();
        this.propagation = annotation.propagation();
    }

    Object proceed() throws Throwable {
        return invocation.proceed();
    }

    Class<? extends Throwable>[] getRollbackFor() {
        return rollbackFor;
    }

    Class<? extends Throwable>[] getNoRollbackFor() {
        return noRollbackFor;
    }

    Propagation getPropagation() {
        return propagation;
    }
}
