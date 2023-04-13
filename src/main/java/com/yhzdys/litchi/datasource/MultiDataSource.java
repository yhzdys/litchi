package com.yhzdys.litchi.datasource;

import com.yhzdys.litchi.connection.Connection;
import com.yhzdys.litchi.connection.TxConnection;
import com.yhzdys.litchi.context.DataSourceContext;
import com.yhzdys.litchi.context.TransactionContext;
import com.yhzdys.litchi.context.TxConnectionContext;
import com.yhzdys.litchi.transaction.TxId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MultiDataSource extends AbstractDataSource implements InitializingBean {

    private Map<String, DataSource> dataSources;

    private String defaultDataSourceKey;

    private DataSource defaultDataSource;

    public final void setDataSources(Map<String, DataSource> dataSources) {
        Map<String, DataSource> map = new HashMap<>(dataSources.size());
        map.putAll(dataSources);
        this.dataSources = Collections.unmodifiableMap(map);
    }

    public final void setDefaultDataSource(String dataSource) {
        this.defaultDataSourceKey = dataSource;
    }

    @Override
    public final java.sql.Connection getConnection() throws SQLException {
        DataSource dataSource = this.determineDataSource();
        java.sql.Connection connection = dataSource.getConnection();
        return this.wrapConnection(dataSource, connection);
    }

    @Override
    public final java.sql.Connection getConnection(String username, String password) throws SQLException {
        DataSource dataSource = this.determineDataSource();
        java.sql.Connection connection = dataSource.getConnection(username, password);
        return this.wrapConnection(dataSource, connection);
    }

    @Override
    public final void afterPropertiesSet() {
        if (dataSources == null) {
            throw new IllegalArgumentException("DataSources is required");
        }
        if (defaultDataSourceKey == null) {
            throw new IllegalArgumentException("Default dataSource is required");
        }
        defaultDataSource = dataSources.get(defaultDataSourceKey);
    }

    private DataSource determineDataSource() {
        String dataSourceKey = DataSourceContext.current();
        DataSource dataSource;
        if (dataSourceKey == null || dataSourceKey.length() < 1) {
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

    private Connection wrapConnection(DataSource dataSource, java.sql.Connection connection) throws SQLException {
        TxId tid = TransactionContext.get();
        // no transaction existed
        if (tid == null) {
            return new Connection(connection);
        }
        TxConnection txConnection = TxConnectionContext.getConnection(tid, dataSource);
        if (txConnection != null) {
            return txConnection;
        }
        txConnection = new TxConnection(connection);
        TxConnectionContext.saveConnection(tid, dataSource, txConnection);
        return txConnection;
    }
}
