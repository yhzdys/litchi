package com.yhzdys.litchi.mybatis;

import com.yhzdys.litchi.annotation.LitchiRouting;
import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.Proxy;

/**
 * {@inheritDoc}
 */
public class LitchiMapperFactoryBean<T> extends MapperFactoryBean<T> {

    public LitchiMapperFactoryBean() {
    }

    public LitchiMapperFactoryBean(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    @Override
    @SuppressWarnings("unchecked ")
    public T getObject() throws Exception {
        T object = super.getObject();
        Class<T> mapper = super.getMapperInterface();
        LitchiRouting annotation = mapper.getAnnotation(LitchiRouting.class);
        String dataSource = annotation == null ? LitchiRouting.DEFAULT : annotation.value();

        LitchiMapperProxy proxy = new LitchiMapperProxy(object, dataSource);
        return (T) Proxy.newProxyInstance(mapper.getClassLoader(), new Class[]{mapper}, proxy);
    }
}
