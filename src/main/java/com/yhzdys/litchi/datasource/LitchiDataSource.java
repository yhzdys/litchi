package com.yhzdys.litchi.datasource;

import com.yhzdys.litchi.annotation.LitchiRouting;
import com.yhzdys.litchi.transaction.TransactionContext;
import com.yhzdys.litchi.transaction.TransactionId;
import com.yhzdys.litchi.transaction.connection.ConnectionContext;
import com.yhzdys.litchi.transaction.connection.ConnectionProxy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

public class LitchiDataSource extends AbstractDataSource implements InitializingBean {

    private Map<String, DataSource> dataSources;

    private String defaultDataSourceKey;

    private DataSource defaultDataSource;

    public final void setDataSources(Map<String, DataSource> dataSources) {
        this.dataSources = Collections.unmodifiableMap(dataSources);
    }

    public void setDefaultDataSource(String dataSource) {
        this.defaultDataSourceKey = dataSource;
    }

    @Override
    public final Connection getConnection() throws SQLException {
        DataSource dataSource = this.determineDataSource();
        Connection connection = dataSource.getConnection();
        return this.determineConnection(dataSource, connection);
    }

    @Override
    public final Connection getConnection(String username, String password) throws SQLException {
        DataSource dataSource = this.determineDataSource();
        Connection connection = dataSource.getConnection(username, password);
        return this.determineConnection(dataSource, connection);
    }

    @Override
    public final void afterPropertiesSet() {
        if (dataSources == null) {
            throw new IllegalArgumentException("DataSources is required");
        }
        if (defaultDataSourceKey != null) {
            defaultDataSource = dataSources.get(defaultDataSourceKey);
        }
    }

    private DataSource determineDataSource() {
        String dataSourceKey = DataSourceContext.peek();
        DataSource dataSource;
        if (LitchiRouting.DEFAULT.equals(dataSourceKey)) {
            dataSource = defaultDataSource;
        } else {
            dataSource = dataSources.get(dataSourceKey);
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine DataSource for key [" + dataSourceKey + "]");
        } else {
            return dataSource;
        }
    }

    private Connection determineConnection(DataSource dataSource, Connection connection) {
        TransactionId tid = TransactionContext.get();
        if (tid == null) {
            return connection;
        }
        ConnectionProxy connectionProxy = ConnectionContext.getConnection(tid, dataSource);
        if (connectionProxy != null) {
            return connectionProxy;
        }
        connectionProxy = new ConnectionProxy(connection);
        ConnectionContext.addConnection(tid, dataSource, connectionProxy);
        return connectionProxy;
    }
}
