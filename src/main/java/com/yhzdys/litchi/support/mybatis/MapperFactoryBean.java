package com.yhzdys.litchi.support.mybatis;

import com.yhzdys.litchi.annotation.RoutingDataSource;

import java.lang.reflect.Proxy;

public class MapperFactoryBean<T> extends org.mybatis.spring.mapper.MapperFactoryBean<T> {

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        Class<T> mapper = super.getMapperInterface();
        RoutingDataSource annotation = mapper.getAnnotation(RoutingDataSource.class);
        String dataSource = annotation == null ? RoutingDataSource.DEFAULT : annotation.value();

        return (T) Proxy.newProxyInstance(
                mapper.getClassLoader(), new Class[]{mapper}, new MapperInvocationHandler(super.getObject(), dataSource)
        );
    }
}
