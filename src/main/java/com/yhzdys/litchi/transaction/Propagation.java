package com.yhzdys.litchi.transaction;

/**
 * Enumeration that represents transaction propagation behaviors
 * for use with the {@link com.yhzdys.litchi.annotation.LitchiTransactional} annotation
 */
public enum Propagation {

    /**
     * 支持当前事务，如果当前没有事务，就新建一个事务。
     */
    REQUIRED,

    /**
     * 支持当前事务，如果当前没有事务，就以非事务方式执行。
     */
    SUPPORTS,

    /**
     * 支持当前事务，如果当前没有事务，就抛出异常。
     */
    MANDATORY,

    /**
     * 新建事务，如果当前存在事务，把当前事务挂起。
     */
    REQUIRES_NEW,

    /**
     * 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
     */
    NOT_SUPPORTED,

    /**
     * 以非事务方式执行，如果当前存在事务，则抛出异常。
     */
    NEVER,
}
