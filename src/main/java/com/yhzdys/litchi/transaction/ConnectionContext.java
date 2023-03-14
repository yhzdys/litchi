package com.yhzdys.litchi.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionContext {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionContext.class);

    private static final ThreadLocal<Map<TransactionId, Map<DataSource, ConnectionProxy>>> HOLDER = ThreadLocal.withInitial(() -> new ConcurrentHashMap<>(4));

    public static void addConnection(TransactionId tid, DataSource dataSource, ConnectionProxy connectionProxy) {
        HOLDER.get()
                .computeIfAbsent(tid, k -> new ConcurrentHashMap<>(4))
                .computeIfAbsent(dataSource, k -> {
                    try {
                        connectionProxy.setAutoCommit(false);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    return connectionProxy;
                });
    }

    public static ConnectionProxy getConnection(TransactionId tid, DataSource dataSource) {
        Map<DataSource, ConnectionProxy> dsmap = HOLDER.get().get(tid);
        if (dsmap == null || dsmap.isEmpty()) {
            return null;
        }
        return dsmap.get(dataSource);
    }

    /**
     * notify transaction result
     */
    public static void notify(TransactionId tid, boolean rollback) {
        Map<TransactionId, Map<DataSource, ConnectionProxy>> tmap = HOLDER.get();
        if (tmap == null || tmap.isEmpty()) {
            return;
        }
        Map<DataSource, ConnectionProxy> dsmap = tmap.get(tid);
        for (ConnectionProxy connection : dsmap.values()) {
            connection.notify(rollback);
        }
        tmap.remove(tid);
    }
}
