package com.yhzdys.litchi.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class TxConnectionWrapper extends ConnectionWrapper {

    private final boolean autoCommit;

    public TxConnectionWrapper(Connection connection) throws SQLException {
        super(connection);
        this.autoCommit = connection.getAutoCommit();
        if (autoCommit) {
            connection.setAutoCommit(false);
        }
    }

    public void notify(boolean rollback) throws SQLException {
        try {
            if (rollback) {
                connection.rollback();
            } else {
                connection.commit();
            }
        } finally {
            connection.setAutoCommit(autoCommit);
            connection.close();
        }
    }

    @Override
    public void commit() {
        // do nothing
    }

    @Override
    public void rollback() {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }
}
