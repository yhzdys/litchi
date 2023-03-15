package com.yhzdys.litchi.datasource;

import java.util.ArrayDeque;
import java.util.Deque;

public class DataSourceContext {

    private static final ThreadLocal<Deque<String>> HOLDER = ThreadLocal.withInitial(ArrayDeque::new);

    private DataSourceContext() {
    }

    /**
     * 当前所使用的数据源
     *
     * @return current datasource
     */
    public static String peek() {
        return HOLDER.get().peek();
    }

    /**
     * 设置当前线程数据源
     *
     * @param dataSource add datasource
     */
    public static void push(String dataSource) {
        HOLDER.get().push(dataSource);
    }

    /**
     * 移除当前数据源
     */
    public static void pop() {
        Deque<String> deque = HOLDER.get();
        deque.pop();
        if (deque.isEmpty()) {
            HOLDER.remove();
        }
    }

    /**
     * 强制清空本地线程
     */
    public static void clear() {
        HOLDER.remove();
    }
}
