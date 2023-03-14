package com.yhzdys.litchi.transaction;

/**
 * 临时挂起的事务
 */
public class SuspendedTransaction {

    private final TransactionId tid;

    public SuspendedTransaction(TransactionId tid) {
        this.tid = tid;
    }

    public TransactionId getTid() {
        return tid;
    }
}
