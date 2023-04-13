package com.yhzdys.litchi.support.spring;

import com.yhzdys.litchi.annotation.MultiTransactional;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class TxPointcut implements Pointcut {

    private final Matcher matcher = new Matcher();

    @Override
    public ClassFilter getClassFilter() {
        return ClassFilter.TRUE;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return matcher;
    }

    private static class Matcher extends StaticMethodMatcher {

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            MultiTransactional annotation = method.getAnnotation(MultiTransactional.class);
            if (annotation == null) {
                if (Proxy.isProxyClass(targetClass)) {
                    return false;
                } else {
                    annotation = AnnotationUtils.findAnnotation(method, MultiTransactional.class);
                }
            }
            return annotation != null;
        }
    }
}
