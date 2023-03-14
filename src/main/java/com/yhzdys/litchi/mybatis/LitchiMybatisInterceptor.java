package com.yhzdys.litchi.mybatis;

import com.yhzdys.litchi.annotation.LitchiRouting;
import com.yhzdys.litchi.datasource.DataSourceContext;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * mybatis插件，支持在mapper上直接定义数据源路由
 */
@Intercepts(value = {
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),
})
public class LitchiMybatisInterceptor implements Interceptor {

    private static final String NULL = "@_NULL";

    /**
     * [mapper, datasource]
     */
    private final Map<String, String> CACHE = new HashMap<>(128);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        String statementId = statement.getId();
        String datasource = CACHE.get(statementId);
        if (datasource == null) {
            synchronized (CACHE) {
                datasource = CACHE.get(statementId);
                if (datasource == null) {
                    Class<?> mapper = Class.forName(statementId.substring(0, statementId.lastIndexOf(".")));
                    LitchiRouting routing = mapper.getAnnotation(LitchiRouting.class);
                    datasource = routing == null ? null : (routing.value() == null ? null : routing.value());
                    CACHE.put(statementId, datasource == null ? NULL : datasource);
                }
            }
        }
        DataSourceContext.push(NULL.equals(datasource) ? null : datasource);
        try {
            return invocation.proceed();
        } finally {
            DataSourceContext.pop();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
