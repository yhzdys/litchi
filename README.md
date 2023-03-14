# 荔枝(Litchi)

## 关于

一款基于Spring+Mybatis开发的轻量化***本地多数据源事务控制***插件，荔枝很好吃😁

## 快速开始

### 多数据源配置

~~~ java
@Bean
public DataSource masterDataSource() {
    // ....
    return dataSource;
}

@Bean
public DataSource slave1DataSource() {
    // ....
    return dataSource;
}

@Bean
public DataSource slave2DataSource() {
    // ....
    return dataSource;
}

/**
 * 荔枝多数据源汇总
 */
@Bean
@Primary
public DataSource litchiDataSource() {
    Map<Object, Object> map = new HashMap<>(4);
    map.put("master", this.masterDataSource());
    map.put("slave1", this.slave1DataSource());
    map.put("slave2", this.slave2DataSource());

    LitchiDataSource dataSource = new LitchiDataSource();
    // 默认数据源
    dataSource.setDefaultTargetDataSource(map.get("master"));
    // 多数据源
    dataSource.setTargetDataSources(map);
    return dataSource;
}

/**
 * 事务切面配置
 */
@Bean
public PointcutAdvisor litchiTransactionAdvisor() {
    return new LitchiTransactionAdvisor();
}

/**
 * mybatis插件
 */
@Bean
public Interceptor litchiMybatisInterceptor() {
    return new LitchiMybatisInterceptor();
}
~~~

### Mybatis数据源切换

~~~java
@LitchiRouting("master")
public interface MasterMapper {
  // ...
}
~~~

~~~java
@LitchiRouting("slave1")
public interface Salve1Mapper {
  // ...
}
~~~

~~~ java
@LitchiRouting("slave2")
public interface Salve2Mapper {
  // ...
}
~~~

### 事务控制

~~~java
@Service
public class PaperMoonService {

    @Resource
    private MasterMapper masterMapper;
    @Resource
    private Salve1Mapper salve1Mapper;
    @Resource
    private Salve2Mapper salve2Mapper;

    @LitchiTransactional(rollbackFor = {BizException.class, RpcException.class}, noRollbackFor = {IgnoreException.class}, propagation = Propagation.REQUIRED)
    public void doService() {
        // ...
        masterMapper.insert();
        // ...
        salve1Mapper.update();
        // ...
        salve2Mapper.delete();
    }
}
~~~

#### 支持的事务传播方式

~~~java
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
~~~



