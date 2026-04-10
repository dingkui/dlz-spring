# DLZ.Spring 完整功能文档

> 深入了解 DLZ.Spring 的所有功能特性和使用方法

---

## 目录

1. [HttpUtil - HTTP 客户端](#1-httputil---http-客户端)
2. [Redis 操作工具](#2-redis-操作工具)
3. [缓存管理](#3-缓存管理)
4. [树形结构工具](#4-树形结构工具)
5. [加密工具集](#5-加密工具集)
6. [Spring 工具类](#6-spring-工具类)
7. [系统工具类](#7-系统工具类)
8. [接口代理扫描](#8-接口代理扫描)
9. [异常体系](#9-异常体系)
10. [配置说明](#10-配置说明)

---

## 1. HttpUtil - HTTP 客户端

### 1.1 核心概念

**HttpUtil** 是基于 Apache HttpClient 封装的 HTTP 客户端工具，提供简洁的 API 进行 HTTP 请求。

**核心组件**：
- `HttpEnum` - HTTP 方法枚举（GET/POST/PUT/DELETE 等）
- `HttpRequestParam` - 请求参数封装
- `HttpUtil` - HTTP 工具类（底层实现）
- `ResponseHandler` - 响应处理器

### 1.2 基本用法

#### GET 请求

```java
// 无参数
String result = HttpEnum.GET.send("https://api.example.com/users");

// 带参数（自动拼接到 URL）
JSONMap params = new JSONMap("page", 1, "size", 10);
String result = HttpEnum.GET.send(url, params);
// 实际请求：https://api.example.com/users?page=1&size=10

// 带请求头
JSONMap headers = new JSONMap("Authorization", "Bearer token");
String result = HttpEnum.GET.send(url, headers, params);
```

#### POST 请求

```java
// Form 表单（默认）
JSONMap params = new JSONMap("name", "张三", "age", 25);
String result = HttpEnum.POST.send(url, params);
// Content-Type: application/x-www-form-urlencoded
// 请求体：name=张三&age=25

// JSON 格式
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
String result = HttpEnum.POST.send(param);
// Content-Type: application/json
// 请求体：{"name":"张三","age":25}

// 文本格式
HttpRequestParam param = HttpRequestParam.createTextReq(url, text);
String result = HttpEnum.POST.send(param);
// Content-Type: text/plain
```


#### 其他 HTTP 方法

```java
// PUT
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
String result = HttpEnum.PUT.send(param);

// DELETE
String result = HttpEnum.DELETE.send(url);

// PATCH
String result = HttpEnum.PATCH.send(param);
```

### 1.3 高级配置

#### 添加请求头

```java
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);

// 单个请求头
param.addHeader("Authorization", "Bearer token");
param.addHeader("Content-Type", "application/json");

// 批量添加
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token");
headers.put("X-Custom-Header", "value");
param.addHeader(headers);
```

#### 超时配置

```java
RequestConfig config = RequestConfig.custom()
    .setSocketTimeout(10000)           // 读取超时：10秒
    .setConnectTimeout(5000)           // 连接超时：5秒
    .setConnectionRequestTimeout(3000) // 请求超时：3秒
    .build();

HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.setRequestConfig(config);
String result = HttpEnum.POST.send(param);
```

#### 字符编码

```java
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.setCharsetNameRequest("UTF-8");   // 请求编码
param.setCharsetNameResponse("UTF-8");  // 响应编码
```

#### 开启日志

```java
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.setShowLog(true);  // 打印请求和响应信息
```

### 1.4 响应处理（重点）

#### 配合 JSONMap 使用（强烈推荐）

**传统方式 vs JSONMap 方式**：

```java
// ❌ 传统方式（20+ 行代码）
String response = HttpEnum.POST.send(url, params);
Map<String, Object> map = JSON.parseObject(response);
Integer code = (Integer) map.get("code");
if (code == null || code != 0) {
    throw new Exception("请求失败");
}
String orderId = null;
if (map.containsKey("data")) {
    Map data = (Map) map.get("data");
    if (data != null && data.containsKey("order")) {
        Map order = (Map) data.get("order");
        if (order != null) {
            orderId = (String) order.get("orderId");
        }
    }
}

// ✅ JSONMap 方式（5 行代码）
JSONMap response = new JSONMap(HttpEnum.POST.send(url, params));
if (response.getInt("code") != 0) {
    throw new Exception("请求失败");
}
String orderId = response.getStr("data.order.orderId");
```


#### JSONMap 核心特性

```java
JSONMap response = new JSONMap(responseStr);

// 1. 深层穿透（一步到位）
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
String city = response.getStr("data.user.profile.address.city");

// 2. 数组访问
String firstTag = response.getStr("tags[0]");
String lastTag = response.getStr("tags[-1]");  // 负索引

// 3. 自动类型转换
Integer age = response.getInt("age");          // "25" → 25
Boolean active = response.getBoolean("active"); // "true" → true
Double price = response.getDouble("price");     // "99.9" → 99.9

// 4. 默认值
String name = response.getStr("name", "匿名");

// 5. 转换为 Bean
User user = response.getObj("data.user", User.class);
```

### 1.5 完整示例

#### 调用第三方 API

```java
public class ThirdPartyApiClient {
    private static final String BASE_URL = "https://api.third-party.com";
    private static final String API_KEY = "your-api-key";
    
    public OrderInfo getOrderInfo(String orderId) {
        String url = BASE_URL + "/orders/" + orderId;
        
        // 1. 构造请求
        HttpRequestParam param = HttpRequestParam.createJsonReq(url, null);
        param.addHeader("Authorization", "Bearer " + API_KEY);
        param.setRequestConfig(RequestConfig.custom()
            .setSocketTimeout(10000)
            .setConnectTimeout(5000)
            .build());
        
        // 2. 发送请求
        String responseStr = HttpEnum.GET.send(param);
        
        // 3. 使用 JSONMap 解析响应
        JSONMap response = new JSONMap(responseStr);
        
        // 4. 检查响应码
        if (response.getInt("code") != 200) {
            String message = response.getStr("message");
            throw new BussinessException("API调用失败：" + message);
        }
        
        // 5. 快速取值
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(response.getStr("data.order.orderId"));
        orderInfo.setAmount(response.getInt("data.order.amount"));
        orderInfo.setStatus(response.getInt("data.order.status"));
        
        return orderInfo;
    }
}
```

### 1.6 最佳实践

#### 统一封装

```java
public class ApiClient {
    private static final String BASE_URL = "https://api.example.com";
    private static final String TOKEN = "your-token";
    
    public static JSONMap post(String path, Object data) {
        String url = BASE_URL + path;
        HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
        param.addHeader("Authorization", "Bearer " + TOKEN);
        param.setRequestConfig(getDefaultConfig());
        
        String response = HttpEnum.POST.send(param);
        return new JSONMap(response);
    }
    
    public static JSONMap get(String path, Map<String, Object> params) {
        String url = BASE_URL + path;
        HttpRequestParam param = HttpRequestParam.createFormReq(url, params);
        param.addHeader("Authorization", "Bearer " + TOKEN);
        
        String response = HttpEnum.GET.send(param);
        return new JSONMap(response);
    }
    
    private static RequestConfig getDefaultConfig() {
        return RequestConfig.custom()
            .setSocketTimeout(10000)
            .setConnectTimeout(5000)
            .build();
    }
}

// 使用
JSONMap result = ApiClient.post("/users", userData);
String userId = result.getStr("data.userId");
```

---

## 2. Redis 操作工具

### 2.1 核心概念

**Redis 工具** 提供完整的 Redis 操作封装，支持字符串、Hash、List、Set 等数据结构。

**核心组件**：
- `JedisExecutor` - Jedis 执行器（底层）
- `IJedisStringExecutor` - 字符串操作接口
- `IJedisHashExecutor` - Hash 操作接口
- `IJedisListExecutor` - List 操作接口
- `IJedisSetExecutor` - Set 操作接口
- `IJedisKeyExecutor` - Key 操作接口


### 2.2 字符串操作

```java
// 设置值
RedisUtil.set("key", "value");
RedisUtil.set("key", "value", 3600);  // 设置过期时间（秒）

// 获取值
String value = RedisUtil.get("key");

// 批量获取
List<String> values = RedisUtil.mget("key1", "key2", "key3");

// 删除
RedisUtil.del("key");

// 判断是否存在
Boolean exists = RedisUtil.exists("key");

// 设置过期时间
RedisUtil.expire("key", 3600);

// 获取剩余时间
Long ttl = RedisUtil.ttl("key");
```

### 2.3 对象操作（自动序列化）

```java
// 设置对象（自动序列化为 JSON）
User user = new User("张三", 25);
RedisUtil.setObj("user:1", user);
RedisUtil.setObj("user:1", user, 3600);  // 带过期时间

// 获取对象（自动反序列化）
User cached = RedisUtil.getObj("user:1", User.class);

// 删除对象
RedisUtil.del("user:1");
```

### 2.4 Hash 操作

```java
// 设置单个字段
RedisUtil.hset("user:1", "name", "张三");
RedisUtil.hset("user:1", "age", "25");

// 获取单个字段
String name = RedisUtil.hget("user:1", "name");

// 设置多个字段
Map<String, String> map = new HashMap<>();
map.put("name", "张三");
map.put("age", "25");
RedisUtil.hset("user:1", map);

// 获取所有字段
Map<String, String> userMap = RedisUtil.hgetAll("user:1");

// 删除字段
RedisUtil.hdel("user:1", "age");

// 判断字段是否存在
Boolean exists = RedisUtil.hexists("user:1", "name");
```

### 2.5 List 操作

```java
// 左侧推入
RedisUtil.lpush("list", "value1", "value2");

// 右侧推入
RedisUtil.rpush("list", "value3", "value4");

// 左侧弹出
String value = RedisUtil.lpop("list");

// 右侧弹出
String value = RedisUtil.rpop("list");

// 获取范围
List<String> list = RedisUtil.lrange("list", 0, -1);  // 获取全部

// 获取长度
Long length = RedisUtil.llen("list");

// 获取指定索引
String value = RedisUtil.lGetIndex("list", 0);
```

### 2.6 Set 操作

```java
// 添加成员
RedisUtil.sadd("set", "member1", "member2");

// 获取所有成员
Set<String> members = RedisUtil.smembers("set");

// 判断是否存在
Boolean exists = RedisUtil.sHasKey("set", "member1");

// 删除成员
RedisUtil.srem("set", "member1");

// 获取成员数量
Long count = RedisUtil.scard("set");
```

### 2.7 Key 操作

```java
// 删除 Key
RedisUtil.del("key");
RedisUtil.del("key1", "key2", "key3");  // 批量删除

// 判断 Key 是否存在
Boolean exists = RedisUtil.exists("key");

// 设置过期时间
RedisUtil.expire("key", 3600);

// 获取剩余时间
Long ttl = RedisUtil.ttl("key");

// 获取 Key 类型
String type = RedisUtil.type("key");

// 模糊查询 Key
Set<String> keys = RedisUtil.keys("user:*");
```

### 2.8 Redis 队列

```java
// 生产者：推送任务
RedisQueue.push("queue:task", taskData);

// 消费者：弹出任务
String taskData = RedisQueue.pop("queue:task");

// 阻塞式消费（超时 10 秒）
String taskData = RedisQueue.bpop("queue:task", 10);
```

### 2.9 最佳实践

#### 缓存模式

```java
public User getUserById(Long userId) {
    String key = "user:" + userId;
    
    // 1. 先查缓存
    User user = RedisUtil.getObj(key, User.class);
    if (user != null) {
        return user;
    }
    
    // 2. 缓存不存在，查询数据库
    user = userMapper.selectById(userId);
    if (user == null) {
        return null;
    }
    
    // 3. 写入缓存（1小时过期）
    RedisUtil.setObj(key, user, 3600);
    
    return user;
}
```

#### 分布式锁

```java
public boolean tryLock(String lockKey, int expireSeconds) {
    String value = UUID.randomUUID().toString();
    Boolean success = RedisUtil.setnx(lockKey, value, expireSeconds);
    return success != null && success;
}

public void unlock(String lockKey) {
    RedisUtil.del(lockKey);
}
```


---

## 3. 缓存管理

### 3.1 核心概念

**缓存管理** 提供多级缓存支持，包括本地缓存（Ehcache）和 Redis 缓存。

**核心组件**：
- `ICache` - 缓存接口
- `CacheEhcache` - Ehcache 实现
- `CacheRedis` - Redis 实现
- `@CacheAnno` - 缓存注解
- `@CacheEvictAnno` - 缓存清除注解
- `CacheAspect` - 缓存切面

### 3.2 基本用法

#### 使用缓存服务

```java
@Autowired
private ICache cache;

// 设置缓存
cache.set("key", value);
cache.set("key", value, 3600);  // 带过期时间

// 获取缓存
Object value = cache.get("key");

// 删除缓存
cache.remove("key");

// 清空缓存
cache.clear();
```

#### 使用注解

```java
@Service
public class UserService {
    
    // 缓存方法结果
    @CacheAnno(key = "user:#{userId}", ttl = 3600)
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    // 清除缓存
    @CacheEvictAnno(key = "user:#{user.id}")
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
```

### 3.3 自定义缓存 Key

```java
@Component
public class CustomCacheKeyMaker implements ICacheKeyIf {
    
    @Override
    public String getKey(Method method, JSONMap paraMap) {
        // 自定义 Key 生成逻辑
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String params = paraMap.toJSONString();
        return className + ":" + methodName + ":" + params;
    }
}
```

---

## 4. 树形结构工具

### 4.1 核心概念

**树形结构工具** 提供列表转树形结构的功能，适用于菜单、组织架构等场景。

**核心组件**：
- `ITree` - 树形接口
- `TreeUtil` - 树形工具类
- `TreeBase` - 树形基类
- `TreeComm` - 树形通用类

### 4.2 基本用法

#### 实体类实现 ITree 接口

```java
public class Menu implements ITree<Long, Menu> {
    private Long id;
    private Long parentId;
    private String name;
    private List<Menu> children;
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public Long getParentId() {
        return parentId;
    }
    
    @Override
    public List<Menu> getChildren() {
        return children;
    }
    
    @Override
    public void setChildren(List<Menu> children) {
        this.children = children;
    }
}
```

#### 转换为树形结构

```java
// 查询所有菜单
List<Menu> menuList = menuMapper.selectList();

// 转换为树形结构
List<Menu> tree = TreeUtil.toTree(menuList);
```

### 4.3 使用 JSONMap

```java
// 查询数据
List<JSONMap> items = ...;

// 转换为树形结构
List<JSONMap> tree = TreeUtil.toTree(items, "id", "parentId");

// 自动创建父节点（如果不存在）
List<JSONMap> tree = TreeUtil.toTree(items, "id", "parentId", true);
```

### 4.4 自定义字段

```java
// 使用 Lambda 表达式指定字段
List<Dept> deptList = deptMapper.selectList();
List<Dept> tree = TreeUtil.toTree(
    deptList,
    Dept::getId,
    Dept::getParentId,
    Dept::getChildren,
    Dept::setChildren
);
```

### 4.5 完整示例

```java
@Service
public class MenuService {
    
    @Autowired
    private MenuMapper menuMapper;
    
    /**
     * 获取菜单树
     */
    public List<Menu> getMenuTree() {
        // 1. 查询所有菜单
        List<Menu> menuList = menuMapper.selectList();
        
        // 2. 转换为树形结构
        List<Menu> tree = TreeUtil.toTree(menuList);
        
        return tree;
    }
    
    /**
     * 获取用户菜单树
     */
    public List<Menu> getUserMenuTree(Long userId) {
        // 1. 查询用户权限菜单
        List<Menu> menuList = menuMapper.selectByUserId(userId);
        
        // 2. 转换为树形结构
        List<Menu> tree = TreeUtil.toTree(menuList);
        
        return tree;
    }
}
```

---

## 5. 加密工具集

### 5.1 MD5 加密

```java
// 字符串 MD5
String md5 = Md5Util.md5("password");

// 字节数组 MD5
byte[] bytes = "password".getBytes();
String md5 = Md5Util.md5(bytes);

// 文件 MD5
File file = new File("/path/to/file");
String md5 = Md5Util.md5(file);
```

### 5.2 Base64 编解码

```java
// 编码
byte[] encoded = Base64.encode("data".getBytes());
String encodedStr = new String(encoded);

// 解码
byte[] decoded = Base64.decode(encoded);
String decodedStr = new String(decoded);
```


### 5.3 RSA 签名验证

```java
// 生成签名
String content = "data to sign";
String privateKey = "your-private-key";
String sign = RSASignature.sign(content, privateKey);

// 验证签名
String publicKey = "your-public-key";
boolean valid = RSASignature.doCheck(content, sign, publicKey);
```

### 5.4 自定义加密

```java
// 加密
String encrypted = EncryptUtil.encode("data", "key");

// 解密
String decrypted = EncryptUtil.decode(encrypted, "key");
```

### 5.5 随机字符串

```java
// 数字
String random = RandomEnum.NUMBER.random(6);  // 6位数字

// 字母
String random = RandomEnum.LETTER.random(8);  // 8位字母

// 数字+字母
String random = RandomEnum.ALL.random(10);  // 10位数字+字母
```

---

## 6. Spring 工具类

### 6.1 SpringHolder - Spring 上下文工具

```java
// 获取 Bean（按类型）
UserService userService = SpringHolder.getBean(UserService.class);

// 获取 Bean（按名称）
UserService userService = SpringHolder.getBean("userService");

// 获取配置
String value = SpringHolder.getProperty("app.name");
String value = SpringHolder.getProperty("app.name", "default");

// 获取 ApplicationContext
ApplicationContext context = SpringHolder.getApplicationContext();

// 发布事件
SpringHolder.publishEvent(new UserCreatedEvent(user));

// 获取 BeanDefinitionRegistry
BeanDefinitionRegistry registry = SpringHolder.getBeanDefinitionRegistry();
```

### 6.2 TokenHolder - Token 管理

```java
// 设置 Token
TokenHolder.set("token-key", tokenInfo);

// 获取 Token
TokenInfo tokenInfo = TokenHolder.get("token-key");

// 清除 Token
TokenHolder.remove("token-key");

// TokenInfo 结构
public class TokenInfo {
    private String tokenStr;      // Token 字符串
    private int expiresIn;        // 过期时间（秒）
    private Object data;          // 附加数据
}
```

---

## 7. 系统工具类

### 7.1 类型转换工具

```java
// Bean 转换
User user = new User("张三", 25);
UserVO userVO = ConvertUtil.convert(user, UserVO.class);

// 带回调
UserVO userVO = ConvertUtil.convert(user, UserVO.class, vo -> {
    vo.setRoleName("管理员");
});

// List 转换
List<User> userList = ...;
List<UserVO> voList = ConvertUtil.convertList(userList, UserVO.class);
```

### 7.2 反射工具

```java
// 对象拷贝
User source = new User("张三", 25);
User target = new User();
Reflections.copy(source, target);

// 获取泛型类型
Type actualType = Reflections.getActualType(type, 0);

// 获取字段
List<Field> fields = Reflections.getFields(User.class);
```

### 7.3 序列化工具

```java
// 序列化
User user = new User("张三", 25);
byte[] bytes = SerializeUtil.serialize(user);

// 反序列化
User user = (User) SerializeUtil.deserialize(bytes);
```

### 7.4 线程工具

```java
// 休眠
ThreadUtil.sleep(1000);  // 休眠 1 秒
ThreadUtil.sleep(TimeUnit.MINUTES, 1);  // 休眠 1 分钟

// ThreadLocal 操作
ThreadUtil.put("key", value);
Object value = ThreadUtil.get("key");
ThreadUtil.remove("key");
Map<String, Object> all = ThreadUtil.getAll();
```

### 7.5 运行时工具

```java
// 获取进程 ID
int pid = RuntimeUtil.getPid();

// 获取运行时间
Duration upTime = RuntimeUtil.getUpTime();

// 获取 JVM 参数
String jvmArgs = RuntimeUtil.getJvmArguments();

// 获取 CPU 核心数
int cpuNum = RuntimeUtil.getCpuNum();
```

### 7.6 文件工具

```java
// 读取文件
String content = FileUtil.readString(file);
byte[] bytes = FileUtil.readBytes(file);

// 写入文件
FileUtil.writeString(file, content);
FileUtil.writeBytes(file, bytes);

// 复制文件
FileUtil.copy(src, dest);

// 删除文件
FileUtil.delete(file);
```

### 7.7 XML 工具

```java
// XML 转 Map
String xml = "<root><name>张三</name><age>25</age></root>";
Map<String, Object> map = XmlUtil.xmlToMap(xml);

// Map 转 XML
Map<String, Object> map = new HashMap<>();
map.put("name", "张三");
map.put("age", 25);
String xml = XmlUtil.mapToXml(map);

// XML 转 Bean
User user = XmlUtil.xmlToBean(xml, User.class);

// Bean 转 XML
String xml = XmlUtil.beanToXml(user);
```

### 7.8 日期计算工具

```java
// 日期加减
Date tomorrow = DateCalcUtil.addDays(new Date(), 1);
Date nextMonth = DateCalcUtil.addMonths(new Date(), 1);
Date nextYear = DateCalcUtil.addYears(new Date(), 1);

// 日期差值
long days = DateCalcUtil.betweenDays(date1, date2);
long hours = DateCalcUtil.betweenHours(date1, date2);
long minutes = DateCalcUtil.betweenMinutes(date1, date2);

// 格式化
String str = DateCalcUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

// 解析
Date date = DateCalcUtil.parse("2024-01-01", "yyyy-MM-dd");
```

---

## 8. 接口代理扫描

### 8.1 核心概念

**接口代理扫描** 提供动态接口代理功能，适用于 RPC 调用、API 网关等场景。

**核心组件**：
- `ApiProxyHandler` - 代理处理器
- `ApiProxyFactory` - 代理工厂
- `ApiRegistryBean` - 注册 Bean
- `DlzSpringScaner` - 扫描器


### 8.2 使用示例

#### 定义接口

```java
@ApiProxy
public interface UserApi {
    
    @ApiMethod("/users/{id}")
    User getUserById(@PathVariable Long id);
    
    @ApiMethod("/users")
    List<User> listUsers(@RequestParam Map<String, Object> params);
}
```

#### 实现代理处理器

```java
public class UserApiProxyHandler extends ApiProxyHandler {
    
    @Override
    public Object done(Class<?> clas, Method method, Object[] args) throws Exception {
        // 获取参数
        JSONMap params = getParaAsMap(method, args);
        
        // 构造请求
        String url = buildUrl(method, params);
        HttpRequestParam param = HttpRequestParam.createJsonReq(url, params);
        
        // 发送请求
        String response = HttpEnum.POST.send(param);
        
        // 解析响应
        JSONMap result = new JSONMap(response);
        
        // 返回结果
        return result.getObj("data", method.getReturnType());
    }
}
```

#### 注册接口

```java
@Configuration
public class ApiProxyConfig {
    
    @Bean
    public ApiRegistryBean apiRegistryBean() {
        ApiRegistryBean bean = new ApiRegistryBean();
        bean.setBasePackage("com.example.api");
        bean.setProxyHandler(new UserApiProxyHandler());
        return bean;
    }
}
```

---

## 9. 异常体系

### 9.1 异常类型

```java
// 业务异常
throw new BussinessException("业务处理失败");

// HTTP 异常
throw new HttpException("HTTP 请求失败");

// 数据库异常
throw new DbException("数据库操作失败");

// 验证异常
throw new ValidateException("参数验证失败");

// 认证异常
throw new CredentialsException("认证失败");

// 远程调用异常
throw new RemoteException("远程调用失败");
```

### 9.2 异常处理

```java
try {
    // 业务逻辑
} catch (BussinessException e) {
    // 业务异常处理
    log.error("业务异常：{}", e.getMessage());
} catch (HttpException e) {
    // HTTP 异常处理
    log.error("HTTP 异常：{}", e.getMessage());
} catch (Exception e) {
    // 其他异常处理
    log.error("系统异常", e);
}
```

---

## 10. 配置说明

### 10.1 application.yml

```yaml
# DLZ 框架配置
dlz:
  fw:
    # 缓存配置
    cache:
      type: redis          # 缓存类型：redis/ehcache
      ttl: 3600           # 默认过期时间（秒）
      key-prefix: "dlz:"  # Key 前缀
    
    # Redis 配置
    redis:
      key-prefix: "dlz:"  # Key 前缀
      database: 0         # 数据库索引

# Spring Redis 配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000
    jedis:
      pool:
        max-active: 8     # 最大连接数
        max-wait: -1      # 最大等待时间
        max-idle: 8       # 最大空闲连接
        min-idle: 0       # 最小空闲连接

# Ehcache 配置（可选）
spring:
  cache:
    type: ehcache
    ehcache:
      config: classpath:ehcache.xml
```

### 10.2 ehcache.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache>
    <diskStore path="java.io.tmpdir/ehcache"/>
    
    <defaultCache
        maxElementsInMemory="10000"
        eternal="false"
        timeToIdleSeconds="120"
        timeToLiveSeconds="120"
        overflowToDisk="true"
        maxElementsOnDisk="10000000"
        diskPersistent="false"
        diskExpiryThreadIntervalSeconds="120"
        memoryStoreEvictionPolicy="LRU"/>
    
    <cache name="userCache"
        maxElementsInMemory="1000"
        eternal="false"
        timeToIdleSeconds="3600"
        timeToLiveSeconds="3600"
        overflowToDisk="false"
        memoryStoreEvictionPolicy="LRU"/>
</ehcache>
```

---

## 11. 常见问题

### Q1: HttpUtil 线程安全吗？

**完全线程安全**。HttpEnum 是线程安全的，可以在多线程环境中使用。

### Q2: Redis 连接池如何配置？

通过 `spring.redis.jedis.pool` 配置连接池参数：
- `max-active`: 最大连接数
- `max-wait`: 最大等待时间
- `max-idle`: 最大空闲连接
- `min-idle`: 最小空闲连接

### Q3: 如何自定义缓存 Key？

实现 `ICacheKeyIf` 接口，自定义 Key 生成逻辑。

### Q4: 树形结构工具支持多级吗？

支持任意多级树形结构。

### Q5: 如何处理 HTTP 超时？

通过 `RequestConfig` 配置超时参数：
```java
RequestConfig config = RequestConfig.custom()
    .setSocketTimeout(10000)
    .setConnectTimeout(5000)
    .build();
param.setRequestConfig(config);
```

---

## 12. 最佳实践总结

### 12.1 HTTP 请求

- ✅ 使用 HttpUtil + JSONMap 组合
- ✅ 统一封装 API 客户端
- ✅ 设置合理的超时时间
- ✅ 开启日志便于调试

### 12.2 Redis 操作

- ✅ 设置合理的过期时间
- ✅ 使用对象序列化简化操作
- ✅ 采用缓存模式提升性能
- ✅ 注意 Key 命名规范

### 12.3 缓存管理

- ✅ 使用注解简化缓存操作
- ✅ 合理设置缓存过期时间
- ✅ 及时清除过期缓存
- ✅ 多级缓存提升性能

### 12.4 树形结构

- ✅ 实体类实现 ITree 接口
- ✅ 使用 TreeUtil 一键转换
- ✅ 支持自定义字段映射

---

<div align="center">

**简洁 | 易用 | 高效**

[返回顶部](#dlzspring-完整功能文档)

</div>
