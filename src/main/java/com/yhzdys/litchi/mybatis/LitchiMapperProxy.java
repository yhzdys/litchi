package com.yhzdys.litchi.mybatis;

import com.yhzdys.litchi.datasource.DataSourceContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LitchiMapperProxy implements InvocationHandler {

    private final Object target;
    private final String dataSource;

    public LitchiMapperProxy(Object target, String dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        DataSourceContext.push(dataSource);
        try {
            return method.invoke(target, args);
        } finally {
            DataSourceContext.pop();
        }
    }
}
