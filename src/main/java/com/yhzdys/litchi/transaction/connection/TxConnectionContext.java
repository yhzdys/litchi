package com.yhzdys.litchi.transaction.connection;

import com.yhzdys.litchi.transaction.TransactionId;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class TxConnectionContext {

    private static final ThreadLocal<Map<TransactionId, Map<DataSource, TxConnection>>> HOLDER = ThreadLocal.withInitial(() -> new HashMap<>(2));

    /**
     * save concurrent datasource connection, thread-unsafe
     *
     * @param tid        transactionId
     * @param dataSource dataSource
     * @param connection connection
     */
    public static void saveConnection(TransactionId tid, DataSource dataSource, TxConnection connection) {
        HOLDER.get()
                .computeIfAbsent(tid, k -> new HashMap<>(2))
                .putIfAbsent(dataSource, connection);
    }

    /**
     * get concurrent datasource connection, thread-unsafe
     *
     * @param tid        transactionId
     * @param dataSource dataSource
     * @return connection
     */
    public static TxConnection getConnection(TransactionId tid, DataSource dataSource) {
        Map<DataSource, TxConnection> dsmap = HOLDER.get().get(tid);
        if (dsmap == null || dsmap.isEmpty()) {
            return null;
        }
        return dsmap.get(dataSource);
    }

    /**
     * notify transaction result
     *
     * @param tid      transaction id
     * @param rollback if {@code true} rollback otherwise commit
     */
    public static void notify(TransactionId tid, boolean rollback) {
        Map<TransactionId, Map<DataSource, TxConnection>> tmap = HOLDER.get();
        if (tmap == null || tmap.isEmpty()) {
            return;
        }
        Map<DataSource, TxConnection> dsmap = tmap.get(tid);
        for (TxConnection connection : dsmap.values()) {
            connection.notify(rollback);
        }
        tmap.remove(tid);
    }
}
