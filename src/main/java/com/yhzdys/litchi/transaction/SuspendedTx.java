package com.yhzdys.litchi.transaction;

/**
 * 临时挂起的事务
 */
public class SuspendedTx {

    private final TxId txId;

    public SuspendedTx(TxId txId) {
        this.txId = txId;
    }

    public TxId getTxId() {
        return txId;
    }
}
