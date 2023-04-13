package com.yhzdys.litchi.datasource;

import com.yhzdys.litchi.connection.ConnectionWrapper;
import com.yhzdys.litchi.connection.TxConnectionWrapper;
import com.yhzdys.litchi.context.DataSourceContext;
import com.yhzdys.litchi.context.TransactionContext;
import com.yhzdys.litchi.context.TxConnectionContext;
import com.yhzdys.litchi.transaction.TxId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
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
    public final Connection getConnection() throws SQLException {
        DataSource dataSource = this.determineDataSource();
        return this.wrapConnection(dataSource, dataSource.getConnection());
    }

    @Override
    public final Connection getConnection(String username, String password) throws SQLException {
        DataSource dataSource = this.determineDataSource();
        return this.wrapConnection(dataSource, dataSource.getConnection(username, password));
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
        String dataSourceKey = DataSourceContext.get();
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

    private ConnectionWrapper wrapConnection(DataSource dataSource, Connection connection) throws SQLException {
        TxId tid = TransactionContext.get();
        // no transaction existed
        if (tid == null) {
            return new ConnectionWrapper(connection);
        }
        TxConnectionWrapper txConnection = TxConnectionContext.get(tid, dataSource);
        if (txConnection != null) {
            return txConnection;
        }
        txConnection = new TxConnectionWrapper(connection);
        TxConnectionContext.set(tid, dataSource, txConnection);
        return txConnection;
    }
}
