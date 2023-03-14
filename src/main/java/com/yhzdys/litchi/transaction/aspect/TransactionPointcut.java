package com.yhzdys.litchi.transaction.aspect;

import com.yhzdys.litchi.annotation.LitchiTransactional;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TransactionPointcut implements Pointcut {

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
            if (AnnotatedElementUtils.hasAnnotation(method, LitchiTransactional.class)) {
                return true;
            }
            if (Proxy.isProxyClass(targetClass)) {
                return false;
            }
            Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            return method != specificMethod && AnnotatedElementUtils.hasAnnotation(specificMethod, LitchiTransactional.class);
        }
    }
}
