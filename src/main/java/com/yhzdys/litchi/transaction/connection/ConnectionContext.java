package com.yhzdys.litchi.transaction.connection;

import com.yhzdys.litchi.transaction.TransactionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionContext {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionContext.class);

    private static final ThreadLocal<Map<TransactionId, Map<DataSource, TransactionConnection>>> HOLDER = ThreadLocal.withInitial(() -> new ConcurrentHashMap<>(4));

    public static void addConnection(TransactionId tid, DataSource dataSource, TransactionConnection transactionConnection) {
        HOLDER.get()
                .computeIfAbsent(tid, k -> new ConcurrentHashMap<>(4))
                .computeIfAbsent(dataSource, k -> {
                    try {
                        transactionConnection.setAutoCommit(false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    return transactionConnection;
                });
    }

    public static TransactionConnection getConnection(TransactionId tid, DataSource dataSource) {
        Map<DataSource, TransactionConnection> dsmap = HOLDER.get().get(tid);
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
        Map<TransactionId, Map<DataSource, TransactionConnection>> tmap = HOLDER.get();
        if (tmap == null || tmap.isEmpty()) {
            return;
        }
        Map<DataSource, TransactionConnection> dsmap = tmap.get(tid);
        for (TransactionConnection connection : dsmap.values()) {
            connection.notify(rollback);
        }
        tmap.remove(tid);
    }
}
