# HttpUtil 使用手册

> 基于 Apache HttpClient 的 HTTP 工具类，提供简洁的 API 进行 HTTP 请求

---

## 快速开始

### 💡 推荐：配合 JSONMap 使用（一行代码搞定）

**HttpUtil + JSONMap = 完美组合**

```java
// ❌ 传统方式：20 行代码，层层判空
String response = HttpEnum.GET.send(url);
Map<String, Object> map = JSON.parseObject(response);
Map data = (Map) map.get("data");
Map order = (Map) data.get("order");
String orderId = (String) order.get("orderId");
Integer amount = (Integer) order.get("amount");

// ✅ JSONMap 方式：1 行代码，深层穿透
JSONMap response = new JSONMap(HttpEnum.GET.send(url));
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
```

**核心优势**：
- 🎯 **深层穿透**：`data.order.orderId` 一步到位
- 🔄 **自动类型转换**：`"123"` 自动转 `Integer 123`
- 🛡️ **空值安全**：路径不存在返回 null，不抛异常
- ⚡ **性能极快**：纳秒级操作，可忽略不计

---

### GET 请求
```java
// 无参数
String result = HttpEnum.GET.send("https://api.example.com/users");

// 带参数
Map<String, Object> params = new HashMap<>();
params.put("page", 1);
params.put("size", 10);
String result = HttpEnum.GET.send("https://api.example.com/users", params);

// 带请求头
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token");
String result = HttpEnum.GET.send("https://api.example.com/users", headers, params);
```

### POST 请求

#### Form 表单提交
```java
Map<String, Object> params = new HashMap<>();
params.put("username", "张三");
params.put("age", 25);
String result = HttpEnum.POST.send("https://api.example.com/users", params);
```

#### JSON 提交
```java
// 方式1：使用 Map
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 25);
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
String result = HttpEnum.POST.send(param);

// 方式2：使用 JSON 字符串
String json = "{\"name\":\"张三\",\"age\":25}";
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, json);
String result = HttpEnum.POST.send(param);

// 方式3：使用 Bean
User user = new User("张三", 25);
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, user);
String result = HttpEnum.POST.send(param);
```

#### 文本提交
```java
String text = "plain text content";
HttpRequestParam<String> param = HttpRequestParam.createTextReq(url, text);
String result = HttpEnum.POST.send(param);
```

### 其他 HTTP 方法
```java
// PUT
HttpEnum.PUT.send(param);

// DELETE
HttpEnum.DELETE.send(url);

// PATCH
HttpEnum.PATCH.send(param);
```

---

## 🚀 JSON 响应处理（重点推荐）

### 场景 1：API 响应解析

#### 传统方式 vs JSONMap 方式

**API 响应示例**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "order": {
      "orderId": "ORDER123",
      "amount": 9900,
      "status": 1
    },
    "user": {
      "userId": "USER456",
      "nickname": "张三"
    }
  }
}
```

**❌ 传统方式（20+ 行代码）**：
```java
String response = HttpEnum.POST.send(url, params);
Map<String, Object> map = JSON.parseObject(response);

Integer code = (Integer) map.get("code");
if (code == null || code != 0) {
    throw new Exception("请求失败");
}

String orderId = null;
Integer amount = null;
String nickname = null;

if (map.containsKey("data")) {
    Map data = (Map) map.get("data");
    if (data != null && data.containsKey("order")) {
        Map order = (Map) data.get("order");
        if (order != null) {
            orderId = (String) order.get("orderId");
            amount = (Integer) order.get("amount");
        }
    }
    if (data != null && data.containsKey("user")) {
        Map user = (Map) data.get("user");
        if (user != null) {
            nickname = (String) user.get("nickname");
        }
    }
}
```

**✅ JSONMap 方式（5 行代码）**：
```java
JSONMap response = new JSONMap(HttpEnum.POST.send(url, params));

if (response.getInt("code") != 0) {
    throw new Exception("请求失败");
}

String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
String nickname = response.getStr("data.user.nickname");
```

**代码量对比**：20+ 行 → 5 行（减少 75%）

---

### 场景 2：深层嵌套数据

**API 响应**：
```json
{
  "data": {
    "user": {
      "profile": {
        "address": {
          "province": "浙江省",
          "city": "杭州市",
          "district": "西湖区"
        }
      }
    }
  }
}
```

**一行代码搞定**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));
String city = response.getStr("data.user.profile.address.city");  // → "杭州市"
```

---

### 场景 3：数组访问

**API 响应**：
```json
{
  "data": {
    "orders": [
      {"orderId": "ORDER1", "amount": 100},
      {"orderId": "ORDER2", "amount": 200},
      {"orderId": "ORDER3", "amount": 300}
    ]
  }
}
```

**快速访问数组元素**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));

// 访问第一个订单
String firstOrderId = response.getStr("data.orders[0].orderId");  // → "ORDER1"

// 访问最后一个订单（负索引）
String lastOrderId = response.getStr("data.orders[-1].orderId");  // → "ORDER3"

// 获取整个订单列表
JSONList orders = response.getList("data.orders");
for (int i = 0; i < orders.size(); i++) {
    JSONMap order = orders.getMap(i);
    String orderId = order.getStr("orderId");
    Integer amount = order.getInt("amount");
}
```

---

### 场景 4：类型自动转换

**API 响应（类型混乱）**：
```json
{
  "data": {
    "age": "25",           // 字符串
    "price": "99.9",       // 字符串
    "active": "true",      // 字符串
    "count": 3.0,          // 浮点数
    "ids": "1,2,3"         // 逗号分隔字符串
  }
}
```

**自动类型转换**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));

Integer age = response.getInt("data.age");           // "25" → 25
Double price = response.getDouble("data.price");     // "99.9" → 99.9
Boolean active = response.getBoolean("data.active"); // "true" → true
Integer count = response.getInt("data.count");       // 3.0 → 3
List<Integer> ids = response.getList("data.ids", Integer.class);  // "1,2,3" → [1,2,3]
```

---

### 场景 5：批量取值

**读取同一对象的多个字段**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));

// 方式1：直接路径访问
String orderId = response.getStr("data.order.orderId");
String transactionId = response.getStr("data.order.transactionId");
Integer status = response.getInt("data.order.status");
Integer amount = response.getInt("data.order.amount");

// 方式2：先获取子对象（推荐，性能更好）
JSONMap order = response.getMap("data.order");
String orderId = order.getStr("orderId");
String transactionId = order.getStr("transactionId");
Integer status = order.getInt("status");
Integer amount = order.getInt("amount");
```

---

### 场景 6：条件判断

**根据响应码处理**：
```java
JSONMap response = new JSONMap(HttpEnum.POST.send(url, params));

// 判断响应码
if (response.getInt("code") == 0) {
    // 成功
    String orderId = response.getStr("data.orderId");
    return orderId;
} else {
    // 失败
    String message = response.getStr("message");
    throw new BussinessException(message);
}
```

**判断数据是否存在**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));

// 方式1：判断是否为 null
String nickname = response.getStr("data.user.nickname");
if (nickname != null) {
    // 昵称存在
}

// 方式2：提供默认值
String nickname = response.getStr("data.user.nickname", "匿名用户");
```

---

### 场景 7：转换为 Bean

**直接转换为 Java 对象**：
```java
JSONMap response = new JSONMap(HttpEnum.GET.send(url));

// 转换整个响应
ApiResponse apiResponse = response.as(ApiResponse.class);

// 转换子对象
User user = response.getObj("data.user", User.class);
Order order = response.getObj("data.order", Order.class);
```

---

### 场景 8：微信/支付宝支付回调

**微信支付回调示例**：
```java
@PostMapping("/wechat/callback")
public String wechatCallback(@RequestBody String xmlData) {
    // 解析 XML 响应（假设已转为 JSON）
    String json = convertXmlToJson(xmlData);
    JSONMap callback = new JSONMap(json);
    
    // 快速取值
    String returnCode = callback.getStr("return_code");
    String resultCode = callback.getStr("result_code");
    String outTradeNo = callback.getStr("out_trade_no");
    String transactionId = callback.getStr("transaction_id");
    Integer totalFee = callback.getInt("total_fee");
    
    if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
        // 支付成功，更新订单状态
        orderService.updateStatus(outTradeNo, transactionId, totalFee);
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>";
    }
    
    return "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
}
```

---

### 场景 9：第三方 API 对接

**完整示例：调用第三方 API**：
```java
public class ThirdPartyApiClient {
    private static final String BASE_URL = "https://api.third-party.com";
    private static final String API_KEY = "your-api-key";
    
    /**
     * 查询订单详情
     */
    public OrderInfo getOrderInfo(String orderId) {
        String url = BASE_URL + "/orders/" + orderId;
        
        // 构造请求
        HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, null);
        param.addHeader("Authorization", "Bearer " + API_KEY);
        param.setRequestConfig(RequestConfig.custom()
            .setSocketTimeout(10000)
            .setConnectTimeout(5000)
            .build());
        
        // 发送请求
        String responseStr = HttpEnum.GET.send(param);
        
        // 使用 JSONMap 解析响应
        JSONMap response = new JSONMap(responseStr);
        
        // 检查响应码
        if (response.getInt("code") != 200) {
            String message = response.getStr("message");
            throw new BussinessException("API调用失败：" + message);
        }
        
        // 快速取值
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(response.getStr("data.order.orderId"));
        orderInfo.setAmount(response.getInt("data.order.amount"));
        orderInfo.setStatus(response.getInt("data.order.status"));
        orderInfo.setCreateTime(response.getStr("data.order.createTime"));
        
        // 或者直接转换
        // OrderInfo orderInfo = response.getObj("data.order", OrderInfo.class);
        
        return orderInfo;
    }
}
```

---

### 💡 JSONMap 核心特性总结

| 特性 | 说明 | 示例 |
|------|------|------|
| **深层穿透** | 点号路径，一步到位 | `response.getStr("data.order.orderId")` |
| **数组访问** | 支持正负索引 | `response.getStr("orders[0].id")` |
| **负索引** | 倒数访问 | `response.getStr("orders[-1].id")` |
| **类型转换** | 自动转换类型 | `"123"` → `Integer 123` |
| **空值安全** | 路径不存在返回 null | 不抛 NPE |
| **默认值** | 提供默认值 | `response.getStr("key", "默认值")` |
| **Bean 转换** | 转换为 Java 对象 | `response.as(User.class)` |
| **性能极快** | 纳秒级操作 | 100 万次仅需 0.2-1.1 毫秒 |

---

## 高级用法

### 自定义请求头
```java
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer token");
param.addHeader("Content-Type", "application/json");
String result = HttpEnum.POST.send(param);

// 批量添加
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer token");
headers.put("X-Custom-Header", "value");
param.addHeader(headers);
```

### 超时配置
```java
RequestConfig config = RequestConfig.custom()
    .setSocketTimeout(10000)           // 读取超时：10秒
    .setConnectTimeout(5000)           // 连接超时：5秒
    .setConnectionRequestTimeout(3000) // 请求超时：3秒
    .build();

HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
param.setRequestConfig(config);
String result = HttpEnum.POST.send(param);
```

### 自定义响应处理
```java
// 返回 JSONMap
HttpRequestParam<JSONMap> param = new HttpRequestParam<>(url, JSONMap.class);
param.setResponseHandler(new ResponseHandler<JSONMap>() {
    @Override
    public JSONMap handle(HttpRequestParam param, int statusCode, HttpResponse response) {
        String body = EntityUtils.toString(response.getEntity());
        return new JSONMap(body);
    }
});
JSONMap result = HttpEnum.POST.send(param);

// 返回自定义 Bean
HttpRequestParam<User> param = new HttpRequestParam<>(url, User.class);
param.setResponseHandler(new ResponseHandler<User>() {
    @Override
    public User handle(HttpRequestParam param, int statusCode, HttpResponse response) {
        String body = EntityUtils.toString(response.getEntity());
        return JacksonUtil.toObj(body, User.class);
    }
});
User user = HttpEnum.POST.send(param);
```

### 字符编码
```java
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
param.setCharsetNameRequest("UTF-8");   // 请求编码
param.setCharsetNameResponse("UTF-8");  // 响应编码
```

### 日志开关
```java
HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
param.setShowLog(true);  // 开启日志，会打印请求和响应信息
```

---

## API 速查

### HttpEnum（HTTP 方法）
| 方法 | 说明 |
|------|------|
| `GET` | GET 请求 |
| `POST` | POST 请求 |
| `PUT` | PUT 请求 |
| `DELETE` | DELETE 请求 |
| `PATCH` | PATCH 请求 |
| `OPTIONS` | OPTIONS 请求 |
| `HEAD` | HEAD 请求 |
| `TRACE` | TRACE 请求 |

### HttpRequestParam（请求参数）

#### 静态工厂方法
```java
// Form 表单请求
HttpRequestParam.createFormReq(url, params);
HttpRequestParam.createFormReq(url, params, ResponseClass.class);

// JSON 请求
HttpRequestParam.createJsonReq(url, data);
HttpRequestParam.createJsonReq(url, data, ResponseClass.class);

// 文本请求
HttpRequestParam.createTextReq(url, text);
HttpRequestParam.createTextReq(url, text, ResponseClass.class);
```

#### 常用方法
```java
param.addHeader(key, value);           // 添加请求头
param.addHeader(headers);              // 批量添加请求头
param.setRequestConfig(config);        // 设置超时配置
param.setCharsetNameRequest(charset);  // 设置请求编码
param.setCharsetNameResponse(charset); // 设置响应编码
param.setShowLog(true);                // 开启日志
param.setResponseHandler(handler);     // 自定义响应处理
```

---

## 常见场景

### 调用第三方 API（推荐使用 JSONMap）

详见 [JSON 响应处理](#-json-响应处理重点推荐) 章节，包含：
- ✅ API 响应解析（代码量减少 75%）
- ✅ 深层嵌套数据访问
- ✅ 数组访问（支持负索引）
- ✅ 类型自动转换
- ✅ 微信/支付宝支付回调
- ✅ 第三方 API 对接完整示例

**快速示例**：
```java
// 一行代码搞定复杂 JSON 解析
JSONMap response = new JSONMap(HttpEnum.POST.send(url, params));
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
```

### 文件上传（multipart/form-data）
```java
// 注意：当前版本不直接支持文件上传
// 建议使用 Spring 的 RestTemplate 或 MultipartFile
```

### 下载文件
```java
// 自定义响应处理器保存文件
HttpRequestParam<File> param = new HttpRequestParam<>(url, File.class);
param.setResponseHandler(new ResponseHandler<File>() {
    @Override
    public File handle(HttpRequestParam param, int statusCode, HttpResponse response) {
        InputStream is = response.getEntity().getContent();
        File file = new File("/path/to/save/file.pdf");
        FileUtils.copyInputStreamToFile(is, file);
        return file;
    }
});
File file = HttpEnum.GET.send(param);
```

---

## 注意事项

1. **默认编码**：UTF-8
2. **默认超时**：无超时限制（建议设置）
3. **连接池**：当前使用默认连接池，高并发场景建议自定义
4. **异常处理**：
   - `SystemException`：系统异常（网络错误、超时等）
   - `HttpException`：HTTP 异常（4xx、5xx 状态码）
   - `BussinessException`：业务异常（自定义）

5. **线程安全**：HttpEnum 是线程安全的，可以在多线程环境中使用

---

## 最佳实践

### 1. 统一封装（推荐使用 JSONMap）
```java
public class ApiClient {
    private static final String BASE_URL = "https://api.example.com";
    private static final String TOKEN = "your-token";
    
    /**
     * POST 请求，返回 JSONMap
     */
    public static JSONMap post(String path, Object data) {
        String url = BASE_URL + path;
        HttpRequestParam<String> param = HttpRequestParam.createJsonReq(url, data);
        param.addHeader("Authorization", "Bearer " + TOKEN);
        param.setRequestConfig(getDefaultConfig());
        
        String response = HttpEnum.POST.send(param);
        return new JSONMap(response);
    }
    
    /**
     * GET 请求，返回 JSONMap
     */
    public static JSONMap get(String path, Map<String, Object> params) {
        String url = BASE_URL + path;
        HttpRequestParam<String> param = HttpRequestParam.createFormReq(url, params);
        param.addHeader("Authorization", "Bearer " + TOKEN);
        param.setRequestConfig(getDefaultConfig());
        
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

// 使用：一行代码搞定
JSONMap result = ApiClient.post("/users", userData);
String userId = result.getStr("data.userId");
```

### 2. 异常处理
```java
try {
    String result = HttpEnum.POST.send(param);
    // 处理结果
} catch (HttpException e) {
    // HTTP 错误（4xx、5xx）
    log.error("HTTP错误：{}", e.getMessage());
} catch (SystemException e) {
    // 系统错误（网络、超时等）
    log.error("系统错误：{}", e.getMessage());
}
```

### 3. 日志记录
```java
// 开发环境：开启日志
if (isDev()) {
    param.setShowLog(true);
}

// 生产环境：关闭日志，使用自定义日志
log.info("请求URL：{}", url);
log.info("请求参数：{}", data);
String result = HttpEnum.POST.send(param);
log.info("响应结果：{}", result);
```

---

<div align="center">

**简洁 | 易用 | 高效**

</div>
