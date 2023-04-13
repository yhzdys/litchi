package com.yhzdys.litchi.context;

import com.yhzdys.litchi.transaction.TxId;

public class TransactionContext {

    private static final ThreadLocal<TxId> HOLDER = new ThreadLocal<>();

    public static TxId get() {
        return HOLDER.get();
    }

    public static TxId set(TxId tid) {
        HOLDER.set(tid);
        return tid;
    }

    public static void remove() {
        HOLDER.remove();
    }
}
