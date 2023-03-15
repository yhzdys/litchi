package com.yhzdys.litchi.transaction;

import com.yhzdys.litchi.exception.TransactionException;
import com.yhzdys.litchi.transaction.connection.ConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransactionExecutor {

    public static final TransactionExecutor INSTANCE = new TransactionExecutor();
    private static final Logger logger = LoggerFactory.getLogger(TransactionExecutor.class);

    private TransactionExecutor() {
    }

    public Object execute(Transaction transaction) throws Throwable {
        Propagation propagation = transaction.getPropagation();
        SuspendedTransaction suspended = null;
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

    private SuspendedTransaction suspend() {
        TransactionId tid = TransactionContext.get();
        if (tid == null) {
            return null;
        }
        TransactionContext.remove();
        return new SuspendedTransaction(tid);
    }

    private Object doExecute(Transaction transaction) throws Throwable {
        if (this.hasTransaction()) {
            return transaction.proceed();
        }
        // 开启事务
        TransactionId tid = TransactionContext.set(new TransactionId());
        boolean rollback = false;
        Object result;
        try {
            result = transaction.proceed();
        } catch (Throwable t) {
            try {
                rollback = this.shouldRollback(transaction, t);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            throw t;
        } finally {
            // 回滚or提交事务
            ConnectionContext.notify(tid, rollback);
            TransactionContext.remove();
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

    private void resume(SuspendedTransaction suspended) {
        if (suspended == null) {
            return;
        }
        TransactionContext.set(suspended.getTid());
    }
}
