package com.yhzdys.litchi.transaction;

import com.yhzdys.litchi.context.TransactionContext;
import com.yhzdys.litchi.context.TxConnectionContext;
import com.yhzdys.litchi.exception.TransactionException;

import java.util.Objects;

public class TxExecutor {

    public static final TxExecutor INSTANCE = new TxExecutor();

    private TxExecutor() {
    }

    public Object execute(Transaction transaction) throws Throwable {
        Propagation propagation = transaction.getPropagation();
        SuspendedTx suspended = null;
        try {
            if (Propagation.REQUIRED == propagation) {
                return this.doExecute(transaction);
            }
            if (Propagation.SUPPORTS == propagation) {
                if (this.hasTransaction()) {
                    return this.doExecute(transaction);
                } else {
                    return transaction.proceed();
                }
            }
            if (Propagation.MANDATORY == propagation) {
                if (this.hasTransaction()) {
                    return this.doExecute(transaction);
                } else {
                    throw new TransactionException("No transaction found propagation[mandatory]");
                }
            }
            if (Propagation.REQUIRES_NEW == propagation) {
                suspended = this.suspend();
                return this.doExecute(transaction);
            }
            if (Propagation.NOT_SUPPORTED == propagation) {
                suspended = this.suspend();
                return transaction.proceed();
            }
            if (Propagation.NEVER == propagation) {
                if (this.hasTransaction()) {
                    throw new TransactionException("Transaction found with propagation[never]");
                } else {
                    return transaction.proceed();
                }
            }
            throw new TransactionException("Transaction propagation[" + propagation + "] not support");
        } finally {
            this.resume(suspended);
        }
    }

    private boolean hasTransaction() {
        return TransactionContext.get() != null;
    }

    private SuspendedTx suspend() {
        TxId tid = TransactionContext.get();
        if (tid == null) {
            return null;
        }
        TransactionContext.remove();
        return new SuspendedTx(tid);
    }

    private Object doExecute(Transaction transaction) throws Throwable {
        if (this.hasTransaction()) {
            return transaction.proceed();
        }
        // 开启事务
        TxId tid = TransactionContext.set(new TxId());
        boolean rollback = false;
        Object result;
        try {
            result = transaction.proceed();
        } catch (Throwable t) {
            try {
                rollback = this.shouldRollback(transaction, t);
            } catch (Exception ignored) {
            }
            throw t;
        } finally {
            TransactionContext.remove();
            // 回滚or提交事务
            TxConnectionContext.notify(tid, rollback);
        }
        return result;
    }

    /**
     * 确认抛出的异常是否需要回滚
     */
    private boolean shouldRollback(Transaction transaction, Throwable t) {
        Class<? extends Throwable>[] noRollbacks = transaction.getNoRollbackFor();
        if (noRollbacks != null) {
            for (Class<? extends Throwable> noRollbackEx : noRollbacks) {
                int depth = this.getThrowableDeep(noRollbackEx, t.getClass());
                if (depth >= 0) {
                    return false;
                }
            }
        }
        Class<? extends Throwable>[] rollbacks = transaction.getRollbackFor();
        if (rollbacks != null) {
            for (Class<? extends Throwable> rollbackEx : rollbacks) {
                int depth = this.getThrowableDeep(rollbackEx, t.getClass());
                if (depth >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getThrowableDeep(Class<? extends Throwable> source, Class<?> threw) {
        if (source == Throwable.class || source == Exception.class) {
            return 0;
        }
        if (threw == Throwable.class) {
            return -1;
        }
        if (Objects.equals(threw, source)) {
            return 0;
        }
        return this.getThrowableDeep(source, threw.getSuperclass());
    }

    private void resume(SuspendedTx suspended) {
        if (suspended == null) {
            return;
        }
        TransactionContext.set(suspended.getTxId());
    }
}
