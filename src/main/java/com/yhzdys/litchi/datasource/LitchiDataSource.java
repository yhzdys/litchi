package com.yhzdys.litchi.datasource;

import com.yhzdys.litchi.transaction.TransactionContext;
import com.yhzdys.litchi.transaction.TransactionId;
import com.yhzdys.litchi.transaction.connection.ConnectionContext;
import com.yhzdys.litchi.transaction.connection.ConnectionProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class LitchiDataSource extends AbstractRoutingDataSource {

    @Override
    public final void setTargetDataSources(Map<Object, Object> dataSources) {
        super.setTargetDataSources(dataSources);
    }

    @Override
    public final DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }

    @Override
    public final String determineCurrentLookupKey() {
        return DataSourceContext.peek();
    }

    @Override
    public final Connection getConnection() throws SQLException {
        DataSource dataSource = this.determineTargetDataSource();
        TransactionId tid = TransactionContext.get();
        // no transactional
        if (tid == null) {
            return dataSource.getConnection();
        }
        ConnectionProxy connection = ConnectionContext.getConnection(tid, dataSource);
        if (connection != null) {
            return connection;
        }
        connection = new ConnectionProxy(dataSource.getConnection());
        ConnectionContext.addConnection(tid, dataSource, connection);
        return connection;
    }
}
