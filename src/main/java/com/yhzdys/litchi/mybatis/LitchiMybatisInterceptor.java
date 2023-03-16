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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final Logger logger = LoggerFactory.getLogger(LitchiMybatisInterceptor.class);

    /**
     * [mapper, datasource]
     */
    private final Map<String, String> CACHE = new ConcurrentHashMap<>(128);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String statementId = ((MappedStatement) invocation.getArgs()[0]).getId();
        String dataSource = CACHE.computeIfAbsent(statementId, k -> {
            String result;
            try {
                Class<?> mapper = Class.forName(statementId.substring(0, statementId.lastIndexOf(".")));
                LitchiRouting annotation = mapper.getAnnotation(LitchiRouting.class);
                result = annotation == null ? LitchiRouting.DEFAULT : annotation.value();
            } catch (Exception e) {
                result = LitchiRouting.DEFAULT;
                logger.error(e.getMessage(), e);
            }
            return result;
        });

        DataSourceContext.push(dataSource);
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
