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
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.Collection;
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
public class LitchiMybatisInterceptor implements Interceptor, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LitchiMybatisInterceptor.class);

    /**
     * [mapper, datasource]
     */
    private final Map<String, String> CACHE = new HashMap<>(128);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String dataSource = CACHE.get(((MappedStatement) invocation.getArgs()[0]).getId());
        if (dataSource == null) {
            dataSource = LitchiRouting.DEFAULT;
        }
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, SqlSessionFactory> factories = event.getApplicationContext().getBeansOfType(SqlSessionFactory.class);
        if (factories.isEmpty()) {
            return;
        }
        for (SqlSessionFactory factory : factories.values()) {
            Collection<Class<?>> mappers = factory.getConfiguration().getMapperRegistry().getMappers();
            for (Class<?> mapper : mappers) {
                LitchiRouting annotation = mapper.getAnnotation(LitchiRouting.class);
                String dataSource = annotation == null ? LitchiRouting.DEFAULT : annotation.value();
                Method[] methods = mapper.getMethods();
                for (Method method : methods) {
                    CACHE.put(mapper.getName() + "." + method.getName(), dataSource);
                }
            }
        }
        logger.info("LitchiRouting caches prepare completed, size={}", CACHE.size());
    }
}
