# HttpUtil 快速入门

> 3 分钟上手，告诉你有什么、怎么用

---

## 核心功能

- **发送请求**：GET、POST、PUT、DELETE 等
- **传参方式**：Parameter（URL参数）、Payload（请求体）
- **数据格式**：Form、JSON、Text
- **响应解析**：配合 JSONMap 快速取值

---

## 1. 基本用法

### GET 请求
```java
// 无参数
String result = HttpEnum.GET.send("https://api.example.com/users");

// 带参数（拼接到 URL）
JSONMap params = new JSONMap("page", 1, "size", 10);
String result = HttpEnum.GET.send(url, params);

// 带请求头
JSONMap params = new JSONMap("Authorization", "Bearer token");
String result = HttpEnum.GET.send(url, headers, params);
```

### POST 请求
```java
// Form 表单（默认）
JSONMap params = new JSONMap("name", "张三", "age", 25);
String result = HttpEnum.POST.send(url, params);

// JSON 格式
HttpRequestParam param = HttpRequestParam.createJsonReq(url, params);
String result = HttpEnum.POST.send(param);
```

---

## 2. 数据传递方式

### 方式 1：URL 参数（Query Parameters）
数据拼接在 URL 中，GET 请求常用

```java
// GET 请求：参数自动拼接到 URL
// 实际请求：https://api.example.com/users?page=1&size=10
JSONMap params = new JSONMap("page", 1, "size", 10);
HttpEnum.GET.send(url, params);
```

### 方式 2：Form 表单（请求体）
数据在请求体中，格式为 `key1=value1&key2=value2`

```java
// POST Form：数据在请求体中
// Content-Type: application/x-www-form-urlencoded
// 请求体：name=张三&age=25
JSONMap params = new JSONMap("name", "张三", "age", 25);
HttpEnum.POST.send(url, params);  // 默认就是 Form 格式
```

### 方式 3：JSON 格式（请求体）
数据在请求体中，格式为 JSON 字符串

```java
// POST JSON：数据在请求体中
// Content-Type: application/json
// 请求体：{"name":"张三","age":25}
JSONMap params = new JSONMap("name", "张三", "age", 25);
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
HttpEnum.POST.send(param);

// 也可以直接传 JSON 字符串
String json = "{\"name\":\"张三\",\"age\":25}";
HttpRequestParam param = HttpRequestParam.createJsonReq(url, json);
HttpEnum.POST.send(param);
```

### 方式 4：文本格式（请求体）
数据在请求体中，格式为纯文本

```java
// POST Text：数据在请求体中
// Content-Type: text/plain
// 请求体：这是一段纯文本
String text = "这是一段纯文本";
HttpRequestParam param = HttpRequestParam.createTextReq(url, text);
HttpEnum.POST.send(param);
```

### 对比总结

| 方式 | 数据位置 | 适用请求 | Content-Type | 示例 |
|------|---------|---------|--------------|------|
| **URL 参数** | URL 中 | GET | N/A | `?page=1&size=10` |
| **Form 表单** | 请求体 | POST | `application/x-www-form-urlencoded` | `name=张三&age=25` |
| **JSON** | 请求体 | POST | `application/json` | `{"name":"张三"}` |
| **文本** | 请求体 | POST | `text/plain` | `纯文本内容` |

---

## 3. 添加请求头
```java
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer token");
param.addHeader("Content-Type", "application/json");
String result = HttpEnum.POST.send(param);
```

---

## 4. 响应解析（重点）

### 传统方式（不推荐）
```java
String response = HttpEnum.POST.send(url, params);
Map map = JSON.parseObject(response);
Map data = (Map) map.get("data");
Map order = (Map) data.get("order");
String orderId = (String) order.get("orderId");  // 20+ 行代码
```

### JSONMap 方式（推荐）
```java
// 一行搞定
JSONMap response = new JSONMap(HttpEnum.POST.send(url, params));
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
```

**核心特性**：
- `data.order.orderId` - 深层穿透，一步到位
- `orders[0].id` - 数组访问
- `orders[-1].id` - 负索引（倒数第一个）
- 自动类型转换：`"123"` → `Integer 123`
- 空值安全：路径不存在返回 null

---

## 5. 完整示例

### 调用第三方 API
```java
// 1. 构造请求
String url = "https://api.example.com/orders";
JSONMap params = new JSONMap(
    "orderId", "ORDER123",
    "amount", 9900
);

HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer " + token);

// 2. 发送请求
String responseStr = HttpEnum.POST.send(param);

// 3. 解析响应（使用 JSONMap）
JSONMap response = new JSONMap(responseStr);

// 4. 快速取值
if (response.getInt("code") == 0) {
    String orderId = response.getStr("data.order.orderId");
    Integer amount = response.getInt("data.order.amount");
    String userId = response.getStr("data.user.userId");
} else {
    String message = response.getStr("message");
    throw new Exception(message);
}
```

---

## 6. 常用配置

### 超时设置
```java
RequestConfig config = RequestConfig.custom()
    .setSocketTimeout(10000)      // 读取超时 10秒
    .setConnectTimeout(5000)      // 连接超时 5秒
    .build();

HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.setRequestConfig(config);
```

### 字符编码
```java
param.setCharsetNameRequest("UTF-8");   // 请求编码
param.setCharsetNameResponse("UTF-8");  // 响应编码
```

### 开启日志
```java
param.setShowLog(true);  // 打印请求和响应
```

---

## 7. API 速查

### HTTP 方法
```java
HttpEnum.GET.send(url);
HttpEnum.POST.send(url, params);
HttpEnum.PUT.send(param);
HttpEnum.DELETE.send(url);
```

### 创建请求参数
```java
// Form 表单
HttpRequestParam.createFormReq(url, params);

// JSON 格式
HttpRequestParam.createJsonReq(url, data);

// 文本格式
HttpRequestParam.createTextReq(url, text);
```

### JSONMap 取值
```java
response.getStr("key");              // 字符串
response.getInt("key");              // 整数
response.getDouble("key");           // 浮点数
response.getBoolean("key");          // 布尔值
response.getMap("key");              // 子对象
response.getList("key");             // 列表
response.getStr("a.b.c");            // 深层路径
response.getStr("arr[0]");           // 数组访问
response.getStr("arr[-1]");          // 负索引
response.getStr("key", "默认值");     // 带默认值
```

---

## 8. 最佳实践

### 统一封装
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

---

## 9. AI 辅助开发

### 给 AI 的提示词

**复制以下提示词给 AI（Cursor、Copilot、ChatGPT 等）**：

````
使用 HttpUtil 和 JSONMap 编写 HTTP 请求代码，遵循以下规则：

## 核心 API 使用方法

### 1. 发送 GET 请求
```java
// 无参数
String result = HttpEnum.GET.send("https://api.example.com/users");

// 带参数（自动拼接到 URL）
JSONMap params = new JSONMap("page", 1, "size", 10);
String result = HttpEnum.GET.send(url, params);
```

### 2. 发送 POST 请求（Form 表单）
```java
// Form 表单方式（默认）
JSONMap params = new JSONMap("name", "张三", "age", 25);
String result = HttpEnum.POST.send(url, params);
```

### 3. 发送 POST 请求（JSON 格式）
```java
// 创建 JSON 请求
JSONMap params = new JSONMap("orderId", "ORDER123", "amount", 9900);
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);

// 添加请求头
param.addHeader("Authorization", "Bearer token");
param.addHeader("Content-Type", "application/json");

// 发送请求
String responseStr = HttpEnum.POST.send(param);
```

### 4. 响应解析（使用 JSONMap）
```java
// 解析响应
JSONMap response = new JSONMap(responseStr);

// 深层路径取值（核心特性）
String orderId = response.getStr("data.order.orderId");
Integer amount = response.getInt("data.order.amount");
String userId = response.getStr("data.user.userId");

// 数组访问
String firstTag = response.getStr("tags[0]");
String lastTag = response.getStr("tags[-1]");  // 负索引

// 自动类型转换
Integer age = response.getInt("age");  // "25" → 25
Boolean active = response.getBoolean("active");  // "true" → true
```

### 5. 完整示例
```java
// 1. 准备数据
String url = "https://api.example.com/orders";
JSONMap params = new JSONMap(
    "orderId", "ORDER123",
    "amount", 9900
);

// 2. 创建请求
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);
param.addHeader("Authorization", "Bearer " + token);

// 3. 发送请求
String responseStr = HttpEnum.POST.send(param);

// 4. 解析响应
JSONMap response = new JSONMap(responseStr);

// 5. 取值
if (response.getInt("code") == 0) {
    String orderId = response.getStr("data.order.orderId");
    Integer amount = response.getInt("data.order.amount");
    System.out.println("订单号：" + orderId + "，金额：" + amount);
} else {
    String message = response.getStr("message");
    System.out.println("失败：" + message);
}
```

## 重要规则

1. **响应解析必须使用 JSONMap**：不要用 JSON.parseObject 或 Gson
2. **取值使用深层路径**：`response.getStr("data.order.orderId")` 而不是层层获取
3. **数组访问**：`response.getStr("orders[0].id")` 支持负索引 `orders[-1].id`
4. **自动类型转换**：`response.getInt("age")` 会自动将 "25" 转为 25
5. **添加请求头**：`param.addHeader(key, value)`
6. **JSON 格式**：使用 `HttpRequestParam.createJsonReq(url, data)`
````

### 常用 AI 提示词模板

**模板 1：调用第三方 API**
````
使用 HttpUtil 调用微信支付 API，发送 JSON 数据，添加 Authorization 请求头，解析响应获取订单号

要求：
1. URL: https://api.weixin.qq.com/pay/unifiedorder
2. 请求方式: POST
3. 数据格式: JSON
4. 请求参数: out_trade_no, total_fee
5. 请求头: Authorization
6. 响应解析: 获取 result.prepay_id
````

**模板 2：处理复杂响应**
````
使用 JSONMap 解析 HTTP 响应，响应格式为：
{
  "code": 0,
  "data": {
    "order": {
      "orderId": "xxx",
      "amount": 9900
    },
    "user": {
      "userId": "xxx"
    }
  }
}

获取 orderId、amount、userId 三个字段
````

**模板 3：完整业务流程**
````
编写一个方法调用支付宝支付接口：
1. URL: https://openapi.alipay.com/gateway.do
2. 请求方式: POST
3. 数据格式: Form 表单
4. 请求参数: app_id, method, charset, sign_type, sign, timestamp, version, biz_content
5. 响应解析: 判断 code 是否为 10000，获取 trade_no
使用 HttpUtil 和 JSONMap 实现
````

---

## 总结

**核心要点**：
1. **发送请求**：`HttpEnum.POST.send(url, params)`
2. **传参方式**：Parameter（URL参数）、Payload（请求体）
3. **数据格式**：Form、JSON、Text
4. **添加请求头**：`param.addHeader(key, value)`
5. **响应解析**：`new JSONMap(response).getStr("data.order.id")`

**记住这个模式**：
```java
// 1. 创建参数
HttpRequestParam param = HttpRequestParam.createJsonReq(url, data);

// 2. 添加配置（可选）
param.addHeader("Authorization", "Bearer token");
param.setRequestConfig(config);

// 3. 发送请求
String response = HttpEnum.POST.send(param);

// 4. 解析响应（使用 JSONMap）
JSONMap result = new JSONMap(response);
String value = result.getStr("data.key");
```

**就这么简单！**
