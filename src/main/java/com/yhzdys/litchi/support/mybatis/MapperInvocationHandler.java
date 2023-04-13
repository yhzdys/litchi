package com.yhzdys.litchi.support.mybatis;

import com.yhzdys.litchi.context.DataSourceContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MapperInvocationHandler implements InvocationHandler {

    private final Object target;
    private final String dataSource;

    public MapperInvocationHandler(Object target, String dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DataSourceContext.set(dataSource);
        try {
            return method.invoke(target, args);
        } finally {
            DataSourceContext.remove();
        }
    }
}
