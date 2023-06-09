package com.yhzdys.litchi.context;

import com.yhzdys.litchi.connection.TxConnectionWrapper;
import com.yhzdys.litchi.transaction.TxId;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TxConnectionContext {

    private static final ThreadLocal<Map<TxId, Map<DataSource, TxConnectionWrapper>>> HOLDER = ThreadLocal.withInitial(() -> new HashMap<>(4));

    /**
     * save concurrent datasource connection, thread-unsafe
     *
     * @param tid        transactionId
     * @param dataSource dataSource
     * @param connection connection
     */
    public static void set(TxId tid, DataSource dataSource, TxConnectionWrapper connection) {
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
    public static TxConnectionWrapper get(TxId tid, DataSource dataSource) {
        Map<DataSource, TxConnectionWrapper> dsmap = HOLDER.get().get(tid);
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
     * @throws SQLException SQLException
     */
    public static void notify(TxId tid, boolean rollback) throws SQLException {
        Map<TxId, Map<DataSource, TxConnectionWrapper>> tmap = HOLDER.get();
        if (tmap == null || tmap.isEmpty()) {
            return;
        }
        SQLException exception = null;
        Map<DataSource, TxConnectionWrapper> dsmap = tmap.get(tid);
        for (TxConnectionWrapper connection : dsmap.values()) {
            try {
                connection.notify(rollback);
            } catch (SQLException e) {
                if (exception == null) {
                    exception = e;
                }
            }
        }
        tmap.remove(tid);
        if (exception != null) {
            throw exception;
        }
    }
}
