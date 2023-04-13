package com.yhzdys.litchi.context;

import java.util.ArrayDeque;
import java.util.Deque;

public class DataSourceContext {

    private static final ThreadLocal<Deque<String>> HOLDER = ThreadLocal.withInitial(() -> new ArrayDeque<>(4));

    private DataSourceContext() {
    }

    /**
     * 当前所使用的数据源
     *
     * @return current datasource
     */
    public static String get() {
        return HOLDER.get().peek();
    }

    /**
     * 设置当前线程数据源
     *
     * @param datasource add datasource
     */
    public static void set(String datasource) {
        HOLDER.get().push(datasource);
    }

    /**
     * 移除当前数据源
     */
    public static void remove() {
        Deque<String> deque = HOLDER.get();
        deque.pop();
        if (deque.isEmpty()) {
            clear();
        }
    }

    /**
     * 强制清空本地线程
     */
    public static void clear() {
        HOLDER.remove();
    }
}
