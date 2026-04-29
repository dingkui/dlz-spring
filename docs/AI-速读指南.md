# DLZ.Spring AI 速读指南

> 3分钟掌握 DLZ.Spring 核心功能，快速上手开发

## 核心定位

**Spring 增强工具集** - 提供 HTTP 客户端、Redis 操作、缓存管理、Spring 工具等常用功能

## 核心模块

```
dlz-spring/
├── HttpUtil          # HTTP 客户端（核心）
├── Redis             # Redis 操作工具
├── Cache             # 缓存管理
├── SpringHolder      # Spring 上下文工具
└── 其他工具类         # 加密、文件、XML 等
```

## 1. HttpUtil - HTTP 客户端（最常用）

### 核心理念

**HttpUtil + JSONMap = 完美组合**
- HttpUtil 负责发送请求
- JSONMap 负责解析响应（深层穿透、自动类型转换）

### 快速上手

```java
// GET 请求
String result = HttpEnum.GET.send("https://api.example.com/users");

// POST 请求（Form 表单）
JSONMap params = new JSONMap("name", "张三", "age", 25);
String result = HttpEnum.POST.send(url, params);

// POST 请求（JSON 格式）
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer token");
String result = HttpEnum.POST.send(param);

// 响应解析（使用 JSONMap）
JSONMap response = new JSONMap(result);
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
```

### 数据传递方式

| 方式 | 数据位置 | 适用请求 | 创建方法 |
|------|---------|---------|---------|
| URL 参数 | URL 中 | GET | `HttpEnum.GET.send(url, params)` |
| Form 表单 | 请求体 | POST | `HttpEnum.POST.send(url, params)` |
| JSON | 请求体 | POST | `HttpRequestParam.createJsonReq(url, data)` |
| 文本 | 请求体 | POST | `HttpRequestParam.createTextReq(url, text)` |

### 完整示例

```java
// 1. 准备数据
String url = "https://api.example.com/orders";
JSONMap data = new JSONMap("orderId", "ORDER123", "amount", 9900);

// 2. 创建请求
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer " + token);
param.setRequestConfig(RequestConfig.custom()
    .setSocketTimeout(10000)
    .setConnectTimeout(5000)
    .build());

// 3. 发送请求
String responseStr = HttpEnum.POST.send(param);

// 4. 解析响应（使用 JSONMap）
JSONMap response = new JSONMap(responseStr);

// 5. 快速取值
if (response.getInt("code") == 0) {
    String orderId = response.getStr("data.order.orderId");
    Integer amount = response.getInt("data.order.amount");
} else {
    String message = response.getStr("message");
    throw new BussinessException(message);
}
```

### HttpUtil API 速查

```java
// HTTP 方法
HttpEnum.GET.send(url)
HttpEnum.POST.send(url, params)
HttpEnum.PUT.send(param)
HttpEnum.DELETE.send(url)

// 创建请求参数
HttpRequestParam.createFormReq(url, params)    // Form 表单
HttpRequestParam.createJsonReq(url, data)      // JSON 格式
HttpRequestParam.createTextReq(url, text)      // 文本格式

// 添加配置
param.addHeader(key, value)                    // 添加请求头
param.setRequestConfig(config)                 // 设置超时
param.setShowLog(true)                         // 开启日志
```

### JSONMap 响应解析（核心特性）

```java
JSONMap response = new JSONMap(responseStr);

// 深层穿透（一步到位）
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");

// 数组访问
String firstTag = response.getStr("tags[0]");
String lastTag = response.getStr("tags[-1]");  // 负索引

// 自动类型转换
Integer age = response.getInt("age");          // "25" → 25
Boolean active = response.getBoolean("active"); // "true" → true

// 默认值
String name = response.getStr("name", "匿名");

// 转换为 Bean
User user = response.getObj("data.user", User.class);
```

### 最佳实践：统一封装

```java
public class ApiClient {
    private static final String BASE_URL = "https://api.example.com";
    
    public static JSONMap post(String path, Object data) {
        HttpRequestParam param = HttpRequestParam.createJsonReq(BASE_URL + path, data);
        param.addHeader("Authorization", "Bearer " + getToken());
        String response = HttpEnum.POST.send(param);
        return new JSONMap(response);
    }
}

// 使用
JSONMap result = ApiClient.post("/orders", orderData);
String orderId = result.getStr("data.orderId");
```

## 2. Redis 操作工具

### RedisUtil - Redis 工具类

```java
// 字符串操作
RedisUtil.set("key", "value");
String value = RedisUtil.get("key");
RedisUtil.setex("key", 3600, "value");  // 设置过期时间

// 对象操作（自动序列化）
RedisUtil.setObj("user:1", user);
User user = RedisUtil.getObj("user:1", User.class);

// Hash 操作
RedisUtil.hset("user:1", "name", "张三");
String name = RedisUtil.hget("user:1", "name");

// List 操作
RedisUtil.lpush("list", "value1", "value2");
List<String> list = RedisUtil.lrange("list", 0, -1);

// Set 操作
RedisUtil.sadd("set", "member1", "member2");
Set<String> set = RedisUtil.smembers("set");

// 删除
RedisUtil.del("key");

// 过期时间
RedisUtil.expire("key", 3600);
Long ttl = RedisUtil.ttl("key");
```

### RedisQueue - Redis 队列

```java
// 生产者
RedisQueue.push("queue:task", taskData);

// 消费者
String taskData = RedisQueue.pop("queue:task");

// 阻塞式消费
String taskData = RedisQueue.bpop("queue:task", 10);  // 超时 10 秒
```

## 3. 缓存管理

### CacheService - 缓存服务

```java
// 设置缓存
CacheService.set("key", value);
CacheService.set("key", value, 3600);  // 过期时间

// 获取缓存
Object value = CacheService.get("key");

// 删除缓存
CacheService.remove("key");

// 清空缓存
CacheService.clear();
```

### @Cacheable 注解（Spring Cache）

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        // 查询数据库
        return userMapper.selectById(userId);
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public void updateUser(User user) {
        // 更新数据库
        userMapper.updateById(user);
    }
}
```

## 4. Spring 工具类

### SpringHolder - Spring 上下文工具

```java
// 获取 Bean
UserService userService = SpringHolder.getBean(UserService.class);
UserService userService = SpringHolder.getBean("userService");

// 获取配置
String value = SpringHolder.getProperty("app.name");
String value = SpringHolder.getProperty("app.name", "default");

// 获取 ApplicationContext
ApplicationContext context = SpringHolder.getApplicationContext();

// 发布事件
SpringHolder.publishEvent(new UserCreatedEvent(user));
```

### TokenHolder - Token 工具

```java
// 设置当前用户 Token
TokenHolder.set(token);

// 获取当前用户 Token
String token = TokenHolder.get();

// 清除 Token
TokenHolder.remove();
```

## 5. 常用工具类

### 加密工具

```java
// MD5
String md5 = EncryUtil.md5("password");

// SHA256
String sha256 = EncryUtil.sha256("password");

// AES 加密/解密
String encrypted = EncryUtil.aesEncrypt("data", "key");
String decrypted = EncryUtil.aesDecrypt(encrypted, "key");

// Base64
String encoded = EncryUtil.base64Encode("data");
String decoded = EncryUtil.base64Decode(encoded);
```

### 文件工具

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

### XML 工具

```java
// XML 转 Map
Map<String, Object> map = XmlUtil.xmlToMap(xml);

// Map 转 XML
String xml = XmlUtil.mapToXml(map);

// XML 转 Bean
User user = XmlUtil.xmlToBean(xml, User.class);

// Bean 转 XML
String xml = XmlUtil.beanToXml(user);
```

### 日期计算工具

```java
// 日期加减
Date tomorrow = DateCalcUtil.addDays(new Date(), 1);
Date nextMonth = DateCalcUtil.addMonths(new Date(), 1);

// 日期差值
long days = DateCalcUtil.betweenDays(date1, date2);
long hours = DateCalcUtil.betweenHours(date1, date2);

// 格式化
String str = DateCalcUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
Date date = DateCalcUtil.parse("2024-01-01", "yyyy-MM-dd");
```

## 6. 异常体系

```java
BussinessException      // 业务异常
HttpException           // HTTP 异常
DbException             // 数据库异常
ValidateException       // 验证异常
CredentialsException    // 认证异常
RemoteException         // 远程调用异常
```

## 开发流程

### 场景 1：调用第三方 API

```java
// 1. 创建请求
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer " + token);

// 2. 发送请求
String responseStr = HttpEnum.POST.send(param);

// 3. 解析响应（使用 JSONMap）
JSONMap response = new JSONMap(responseStr);
String orderId = response.getStr("data.order.orderId");
```

### 场景 2：Redis 缓存

```java
// 查询时先查缓存
User user = RedisUtil.getObj("user:" + userId, User.class);
if (user == null) {
    // 缓存不存在，查询数据库
    user = userMapper.selectById(userId);
    // 写入缓存
    RedisUtil.setObj("user:" + userId, user, 3600);
}
return user;
```

### 场景 3：异步任务队列

```java
// 生产者：推送任务
RedisQueue.push("queue:email", emailTask);

// 消费者：处理任务
@Scheduled(fixedDelay = 1000)
public void processEmailTask() {
    String taskData = RedisQueue.pop("queue:email");
    if (taskData != null) {
        // 处理任务
        sendEmail(taskData);
    }
}
```

## 配置说明

### application.yml

```yaml
# Redis 配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000
    jedis:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0

# 缓存配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

## 注意事项

1. **HttpUtil 响应解析** - 强烈推荐使用 JSONMap，代码量减少 75%
2. **Redis 序列化** - 对象自动序列化为 JSON，无需手动处理
3. **缓存过期时间** - 合理设置过期时间，避免内存溢出
4. **异常处理** - 使用统一的异常体系，便于全局处理
5. **线程安全** - 所有工具类都是线程安全的

## 核心优势

- **HttpUtil + JSONMap** - 完美组合，HTTP 请求和响应解析一气呵成
- **深层穿透** - `response.getStr("data.order.orderId")` 一步到位
- **自动类型转换** - `"123"` 自动转 `Integer 123`
- **Redis 简化** - 对象自动序列化，无需手动处理
- **Spring 增强** - SpringHolder 提供便捷的 Bean 获取和配置读取

## 快速对比

### 传统方式 vs DLZ.Spring

```java
// ❌ 传统方式（20+ 行代码）
String response = restTemplate.postForObject(url, request, String.class);
Map map = JSON.parseObject(response);
Map data = (Map) map.get("data");
Map order = (Map) data.get("order");
String orderId = (String) order.get("orderId");

// ✅ DLZ.Spring 方式（3 行代码）
String response = HttpEnum.POST.send(url, params);
JSONMap result = new JSONMap(response);
String orderId = result.getStr("data.order.orderId");
```

---

**记住**: HttpUtil + JSONMap = 完美组合，让 HTTP 请求和响应解析变得简单高效！
