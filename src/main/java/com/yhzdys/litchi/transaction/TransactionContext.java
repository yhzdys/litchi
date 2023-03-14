package com.yhzdys.litchi.transaction;

public class TransactionContext {

    private static final ThreadLocal<TransactionId> HOLDER = new ThreadLocal<>();

    public static TransactionId get() {
        return HOLDER.get();
    }

    public static TransactionId set(TransactionId tid) {
        HOLDER.set(tid);
        return tid;
    }

    public static void remove() {
        HOLDER.remove();
    }
}
