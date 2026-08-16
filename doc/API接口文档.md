# API 接口文档

## 基础信息

- **API 版本**：`v1`
- **Base URL**：`https://api.pay.example.com/v1`
- **内容类型**：`application/json`
- **字符编码**：`UTF-8`
- **证书**：商户使用 `appid` + `签名` 进行身份验证

---

## 认证方式

### 签名认证

```http
POST /v1/pay/order/create
Content-Type: application/json
X-Appid: app_xxxxxxxx
X-Timestamp: 1628012345
X-Noncece: a1b2c3d4e5
X-Signature: SHA256(app_xxxxxxxx + 1628012345 + a1b2c3d4e5 + request_body + secret_key)

{
  "merchant_order_no": "MORD_001",
  "amount": 10000,
  "currency": "CNY",
  "channel_pref": ["wx_jsapi"],
  ...
}
```

- **签名算法**：`SHA256(appid + timestamp + nonce + request_body + secret_key)`
- **secret_key**：在应用创建后分配，存储在应用配置中
- **Timestamp**：秒级 Unix 时间戳，请求超时 5 分钟
- **Nonce**：随机字符串，防止重放攻击

---

## 错误响应格式

```json
{
  "code": "INVALID_PARAMETER",
  "message": "金额不能大于50000元",
  "detail": {
    "field": "amount",
    "max": 50000
  },
  "request_id": "req_1628012345_abcdef",
  "timestamp": 1628012345
}
```

### 通用错误码

| 错误码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| `INVALID_PARAMETER` | 400 | 请求参数错误 |
| `INVALID_SIGNATURE` | 401 | 签名验证失败 |
| `EXPIRED_REQUEST` | 401 | 请求已过期 |
| `UNAUTHORIZED` | 401 | 未授权 |
| `APP_NOT_FOUND` | 404 | 应用不存在 |
| `APP_DISABLED` | 403 | 应用已停用 |
| `CHANNEL_NOT_CONFIGURED` | 403 | 通道未配置 |
| `CHANNEL_NOT_AVAILABLE` | 403 | 通道不可用 |
| `STRATEGY_NOT_FOUND` | 404 | 策略未找到 |
| `INSUFFICIENT_FUNDS` | 402 | 余额不足 / 超出额度 |
| `PAYMENT_FAILED` | 402 | 支付失败 |
| `REFUND_FAILED` | 402 | 退款失败 |
| `TRANSACTION_NOT_FOUND` | 404 | 交易记录未找到 |
| `USER_NOT_AUTHORIZED` | 403 | 用户未授权 |
| `RATE_LIMIT_EXCEEDED` | 429 | 超出调用频率限制 |
| `INTERNAL_ERROR` | 500 | 服务器内部错误 |

---

## 一、商户/应用管理接口

### 1.1 商户注册

```
POST /merchant/register
```

**请求体**：

```json
{
  "name": "张三的店",
  "company_name": "张三科技有限公司",
  "unified_code": "91110000MA12345678",
  "legal_person": "张三",
  "legal_id_card": "110101198001011234",
  "contact_name": "李四",
  "contact_phone": "13800138000",
  "contact_email": "li.si@example.com"
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "merchant_id": 10001,
    "status": "PENDING"
  }
}
```

### 1.2 提交认证材料

```
POST /merchant/{merchant_id}/certification
```

**请求体**（多部分上传，文件通过单独 API 上传）：

```json
{
  "business_license_front": "file_id_001",
  "business_license_back": "file_id_002",
  "legal_id_card_front": "file_id_003",
  "legal_id_card_back": "file_id_004",
  "authorization_letter": "file_id_005"
}
```

### 1.3 创建应用

```
POST /app
```

**请求体**：

```json
{
  "merchant_id": 10001,
  "name": "电商主站支付",
  "type": 1,
  "callback_url": "https://merchant.example.com/pay/callback",
  "return_url": "https://merchant.example.com/pay/return"
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "app_id": 10001,
    "appid": "app_16280123_a7f3",
    "secret_key": "***",
    "status": "DRAFT"
  }
}
```

> **注意**：`secret_key` 仅在创建时返回一次，请妥善保存。

### 1.4 获取应用详情

```
GET /app/{app_id}
```

### 1.5 更新应用

```
PUT /app/{app_id}
```

### 1.6 获取应用列表

```
GET /app
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| merchant_id | BIGINT | 商户 ID（必须） |
| status | TINYINT | 状态筛选 |
| page | INT | 页码，默认 1 |
| size | INT | 每页数量，默认 20 |

---

## 二、支付通道管理接口

### 2.1 获取可用通道列表

```
GET /channel/available
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": [
    {
      "code": "wx_jsapi",
      "name": "微信支付 JSAPI",
      "scenes": ["ecommerce", "app", "h5"],
      "status": "AVAILABLE",
      "description": "支持微信 APP 内 H5 页面支付"
    },
    {
      "code": "alipay_trade",
      "name": "支付宝电脑网站支付",
      "scenes": ["ecommerce", "h5"],
      "status": "AVAILABLE",
      "description": "PC 端支付"
    },
    {
      "code": "zj_payment",
      "name": "中金支付",
      "scenes": ["ecommerce", "app"],
      "status": "AVAILABLE",
      "description": "综合支付方案"
    },
    {
      "code": "bf_payment",
      "name": "宝付支付",
      "scenes": ["ecommerce", "live"],
      "status": "AVAILABLE",
      "description": "直播场景支付"
    }
  ]
}
```

### 2.2 选择通道（关联到应用）

```
POST /app/{app_id}/channel/select
```

**请求体**：

```json
{
  "channel_codes": ["wx_jsapi", "alipay_trade", "zj_payment"]
}
```

### 2.3 配置渠道实例

```
POST /app/{app_id}/channel/instance
```

**请求体**：

```json
{
  "channel_code": "wx_jsapi",
  "instance_name": "微信 JSAPI 主实例",
  "config": {
    "appid": "wx1234567890",
    "mch_id": "1234567890",
    "secret": "***",
    "cert_path": "***",
    "key_path": "***"
  },
  "fees": {
    "rate": 0.006,
    "caps": {
      "min": 1,
      "max": 50000
    }
  },
  "amount_limit": {
    "single_max": 50000,
    "daily_max": 500000
  }
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "instance_id": 20001,
    "status": "UNCONFIGURED"  // 或配置完成后返回 TESTING
  }
}
```

### 2.4 测试渠道实例连通性

```
POST /app/{app_id}/channel/instance/{instance_id}/test
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "test_result": {
      "status": "CONNECTED",
      "latency_ms": 120,
      "details": "测试成功"
    }
  }
}
```

### 2.5 获取渠道实例列表

```
GET /app/{app_id}/channel/instance
```

### 2.6 删除渠道实例

```
DELETE /app/{app_id}/channel/instance/{instance_id}
```

> 注意：已关联到策略中的实例不能删除，需先从策略中移除。

---

## 三、支付策略接口

### 3.1 创建策略草稿

```
POST /app/{app_id}/strategy/draft
```

**请求体**：

```json
{
  "name": "电商默认策略",
  "description": "按金额区段路由",
  "rules": [
    {
      "priority": 1,
      "name": "低金额微信优先",
      "description": "订单金额 ≤100 元走微信",
      "condition": {
        "amount_range": [0, 100],
        "scene": "ecommerce"
      },
      "channels": ["wx_jsapi", "alipay_trade"],
      "sort_by": "fee_rate_asc"
    },
    {
      "priority": 2,
      "name": "中高金额支付宝优先",
      "description": "订单金额 100-10000 元走支付宝",
      "condition": {
        "amount_range": [100, 10000],
        "scene": "ecommerce"
      },
      "channels": ["alipay_trade", "wx_jsapi", "zj_payment"],
      "sort_by": "fee_rate_asc"
    },
    {
      "priority": 3,
      "name": "大额中金支付",
      "description": "订单金额 >10000 元走中金",
      "condition": {
        "amount_range": [10000, null],
        "scene": "ecommerce"
      },
      "channels": ["zj_payment", "alipay_trade"],
      "sort_by": "limit_first"
    }
  ],
  "fallback_policy": {
    "enabled": true,
    "max_attempts": 3,
    "require_re_authorization": false
  }
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "strategy_id": 30001,
    "status": "DRAFT"
  }
}
```

### 3.2 获取策略草稿列表

```
GET /app/{app_id}/strategy/draft
```

### 3.3 获取策略草稿详情

```
GET /app/{app_id}/strategy/draft/{strategy_id}
```

### 3.4 更新策略草稿

```
PUT /app/{app_id}/strategy/draft/{strategy_id}
```

### 3.5 删除策略草稿

```
DELETE /app/{app_id}/strategy/draft/{strategy_id}
```

### 3.6 发布策略

```
POST /app/{app_id}/strategy/draft/{strategy_id}/publish
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "strategy_id": 30001,
    "status": "PUBLISHED",
    "version": 1,
    "published_at": "2026-08-16T10:30:00Z"
  }
}
```

### 3.7 获取已发布策略

```
GET /app/{app_id}/strategy/published
```

### 3.8 停用策略

```
POST /app/{app_id}/strategy/published/{strategy_id}/disable
```

---

## 四、支付接口

### 4.1 创建支付订单

```
POST /pay/order/create
```

**签名认证**：需要 `appid` + `签名`

**请求体**：

```json
{
  "merchant_order_no": "MORD_20260816_001",
  "amount": 10000,
  "currency": "CNY",
  "channel_pref": ["wx_jsapi", "alipay_trade"],
  "scene": "ecommerce",
  "risk_level": 0,
  "product_name": "智能充电器",
  "product_desc": "10W 快充充电器，白色",
  "metadata": {
    "client_ip": "123.123.123.123",
    "user_agent": "Mozilla/5.0...",
    "device_type": "mobile"
  },
  "callback_params": {
    "custom_field_1": "value1"
  }
}
```

**请求参数说明**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchant_order_no | VARCHAR(64) | 是 | 商户订单号，用于幂等 |
| amount | DECIMAL(16,2) | 是 | 支付金额（分） |
| currency | VARCHAR(8) | 否 | 货币代码，默认 CNY |
| channel_pref | ARRAY | 否 | 商户指定通道 preference（可选，最终由路由引擎决定） |
| scene | VARCHAR(32) | 是 | 支付场景 |
| risk_level | TINYINT | 否 | 风控等级，默认 0 |
| product_name | VARCHAR(128) | 是 | 商品名称 |
| product_desc | VARCHAR(512) | 否 | 商品描述 |
| metadata | JSON | 否 | 扩展信息 |
| callback_params | JSON | 否 | 回调时透传的参数 |

**响应 — 需要授权**：

```json
{
  "code": "NEED_AUTHORIZATION",
  "message": "用户需要进行综合授权",
  "data": {
    "order_id": 100001,
    "order_no": "pay_1628012345_xxxxxx",
    "amount": 10000,
    "currency": "CNY",
    "status": "PENDING_AUTH",
    "auth_needed": {
      "app_id": 10001,
      "user_id": 50001,
      "channels": [
        {"code": "wx_jsapi", "name": "微信支付"},
        {"code": "alipay_trade", "name": "支付宝支付"},
        {"code": "zj_payment", "name": "中金支付"}
      ]
    },
    "created_at": "2026-08-16T10:30:00Z"
  }
}
```

**响应 — 已授权，可直接支付**：

```json
{
  "code": "SUCCESS",
  "data": {
    "order_id": 100001,
    "order_no": "pay_1628012345_xxxxxx",
    "amount": 10000,
    "currency": "CNY",
    "status": "PENDING_PAY",
    "pay_info": {
      "channel_code": "wx_jsapi",
      "channel_name": "微信支付 JSAPI",
      "pay_type": "jsapi",
      "pay_data": {
        "appid": "wx1234567890",
        "partnerid": "1234567890",
        "prepayid": "wx2016092310143031981",
        "nonce_str": "random_string",
        "timestamp": "1628012345",
        "package": "Sign=WXPay",
        "sign": "SHA256_HASH"
      }
    },
    "created_at": "2026-08-16T10:30:00Z"
  }
}
```

> `pay_info` 中的内容因通道而异，返回具体的第三方支付参数，商户应用直接使用这些参数调用第三方 SDK。

### 4.2 获取订单状态

```
GET /pay/order/{order_no}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "order_id": 100001,
    "order_no": "pay_1628012345_xxxxxx",
    "merchant_order_no": "MORD_20260816_001",
    "amount": 10000,
    "currency": "CNY",
    "status": "SUCCESS",
    "channel_code": "wx_jsapi",
    "third_order_no": "wx2016092310143031981",
    "scene": "ecommerce",
    "risk_level": 0,
    "paid_at": "2026-08-16T10:30:05Z",
    "created_at": "2026-08-16T10:30:00Z"
  }
}
```

### 4.3 支付结果查询（幂等）

```
POST /pay/order/query
```

**请求体**：

```json
{
  "order_no": "pay_1628012345_xxxxxx"
}
```

### 4.4 用户综合授权

```
POST /auth/comprehensive
```

**请求体**：

```json
{
  "app_id": 10001,
  "user_id": 50001,
  "merchant_order_no": "MORD_20260816_001",
  "channels": [
    "wx_jsapi",
    "alipay_trade",
    "zj_payment"
  ],
  "agreement": {
    "ip": "123.123.123.123",
    "ua": "Mozilla/5.0...",
    "timestamp": 1628012345
  }
}
```

**响应 — 全部授权成功**：

```json
{
  "code": "SUCCESS",
  "data": {
    "auth_result": {
      "wx_jsapi": "AUTHORIZED",
      "alipay_trade": "AUTHORIZED",
      "zj_payment": "AUTHORIZED"
    },
    "all_authorized": true,
    "auth_time": "2026-08-16T10:30:02Z"
  }
}
```

**响应 — 部分授权成功**：

```json
{
  "code": "PARTIAL_AUTHORIZED",
  "data": {
    "auth_result": {
      "wx_jsapi": "AUTHORIZED",
      "alipay_trade": "AUTHORIZED",
      "zj_payment": "FAILED"
    },
    "all_authorized": false,
    "failed_channels": ["zj_payment"],
    "auth_time": "2026-08-16T10:30:02Z"
  }
}
```

### 4.5 查询用户授权状态

```
GET /auth/status
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| app_id | BIGINT | 应用 ID |
| user_id | BIGINT | 用户 ID |

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "auth_status": {
      "wx_jsapi": "AUTHORIZED",
      "alipay_trade": "AUTHORIZED",
      "zj_payment": "NOT_AUTHORIZED"
    }
  }
}
```

### 4.6 解除用户授权

```
POST /auth/revoke
```

**请求体**：

```json
{
  "app_id": 10001,
  "user_id": 50001,
  "channel_code": "wx_jsapi"
}
```

### 4.7 支付执行（内部调用，商户一般不直接调用此接口）

```
POST /pay/execute
```

> 此接口供路由引擎内部使用，用于向具体通道发送支付请求。

---

## 五、退款相关接口

### 5.1 发起退款

```
POST /pay/refund
```

**签名认证**：需要 `appid` + `签名`

**请求体**：

```json
{
  "merchant_refund_no": "MREF_20260816_001",
  "order_no": "pay_1628012345_xxxxxx",
  "amount": 10000,
  "reason": "客户申请退款",
  "callback_params": {}
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "refund_id": 20001,
    "refund_no": "ref_1628012345_yyyyyy",
    "order_no": "pay_1628012345_xxxxxx",
    "amount": 10000,
    "status": "PROCESSING",
    "channel_code": "wx_jsapi",
    "third_refund_no": null,
    "created_at": "2026-08-16T10:35:00Z"
  }
}
```

### 5.2 查询退款状态

```
GET /pay/refund/{refund_no}
```

---

## 六、对账接口

### 6.1 触发对账

```
POST /reconciliation/trigger
```

**请求体**：

```json
{
  "app_id": 10001,
  "channel_code": "wx_jsapi",
  "start_date": "2026-08-01",
  "end_date": "2026-08-15"
}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "task_id": 40001,
    "task_no": "recon_20260816_001",
    "status": "QUEUED",
    "estimated_start": "2026-08-16T11:00:00Z"
  }
}
```

### 6.2 获取对账任务列表

```
GET /reconciliation/task
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| app_id | BIGINT | 应用 ID |
| status | TINYINT | 任务状态 |
| page | INT | 页码 |
| size | INT | 每页数量 |

### 6.3 获取对账任务详情

```
GET /reconciliation/task/{task_id}
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "task_id": 40001,
    "task_no": "recon_20260816_001",
    "app_id": 10001,
    "channel_code": "wx_jsapi",
    "start_date": "2026-08-01",
    "end_date": "2026-08-15",
    "status": "COMPLETED",
    "total_count": 500,
    "success_count": 498,
    "diff_count": 2,
    "started_at": "2026-08-16T11:00:00Z",
    "finished_at": "2026-08-16T11:05:00Z"
  }
}
```

### 6.4 获取对账差异明细

```
GET /reconciliation/task/{task_id}/diff
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": {
    "diffs": [
      {
        "transaction_id": 10001,
        "order_no": "pay_1628012345_xxxxxx",
        "third_order_no": "wx2016092310143031981",
        "platform_amount": 10000,
        "third_amount": 9999,
        "platform_status": "SUCCESS",
        "third_status": "SUCCESS",
        "match_result": "AMOUNT_MISMATCH",
        "diff_detail": "平台金额：100.00 元，第三方金额：99.99 元"
      }
    ]
  }
}
```

### 6.5 获取对账报告（下载）

```
GET /reconciliation/task/{task_id}/report
```

**响应**：返回 CSV/Excel 文件

---

## 七、回调通知（第三方 → 平台）

### 7.1 支付回调

```
POST /callback/pay/{appid}
```

**请求**：第三方支付通道发送的回调请求

**响应**：

```json
{
  "code": "SUCCESS",
  "message": "回调接收成功"
}
```

> 具体的回调参数由各通道定义，平台会进行统一解析和验证。

### 7.2 平台转发回调给商户（可选）

商户可以在应用配置中开启「平台代为转发回调」，平台在接收到第三方回调并验证后，将通知转发给商户的 `callback_url`。

---

## 八、管理/监控接口

### 8.1 获取通道健康状态

```
GET /admin/channel/health
```

**响应**：

```json
{
  "code": "SUCCESS",
  "data": [
    {
      "code": "wx_jsapi",
      "name": "微信支付 JSAPI",
      "status": "HEALTHY",
      "latency_ms": 120,
      "last_check": "2026-08-16T10:30:00Z",
      "error_rate": 0.001
    },
    {
      "code": "alipay_trade",
      "name": "支付宝电脑网站支付",
      "status": "DEGRADED",
      "latency_ms": 3000,
      "last_check": "2026-08-16T10:30:00Z",
      "error_rate": 0.05
    }
  ]
}
```

### 8.2 获取交易统计

```
GET /admin/trade/stat
```

**查询参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| app_id | BIGINT | 应用 ID |
| channel_code | VARCHAR | 通道编码 |
| start_date | DATE | 起始日期 |
| end_date | DATE | 结束日期 |
| group_by | VARCHAR | 统计维度：day, channel |

### 8.3 获取商户年度/月度结算数据

```
GET /admin/settlement/stat
```

---

## API 接口矩阵

| 类别 | 方法 | 接口 | 说明 |
|------|------|------|------|
| 商户 | POST | /merchant/register | 商户注册 |
| 商户 | POST | /merchant/{id}/certification | 提交认证材料 |
| 应用 | POST | /app | 创建应用 |
| 应用 | GET | /app/{id} | 获取应用详情 |
| 应用 | PUT | /app/{id} | 更新应用 |
| 应用 | GET | /app | 获取应用列表 |
| 通道 | GET | /channel/available | 获取可用通道列表 |
| 通道 | POST | /app/{id}/channel/select | 选择通道 |
| 通道 | POST | /app/{id}/channel/instance | 创建渠道实例 |
| 通道 | POST | /app/{id}/channel/instance/{ins_id}/test | 测试实例 |
| 通道 | GET | /app/{id}/channel/instance | 获取实例列表 |
| 通道 | DELETE | /app/{id}/channel/instance/{ins_id} | 删除实例 |
| 策略 | POST | /app/{id}/strategy/draft | 创建策略草稿 |
| 策略 | GET | /app/{id}/strategy/draft | 获取草稿列表 |
| 策略 | GET | /app/{id}/strategy/draft/{sid} | 获取草稿详情 |
| 策略 | PUT | /app/{id}/strategy/draft/{sid} | 更新草稿 |
| 策略 | DELETE | /app/{id}/strategy/draft/{sid} | 删除草稿 |
| 策略 | POST | /app/{id}/strategy/draft/{sid}/publish | 发布策略 |
| 策略 | GET | /app/{id}/strategy/published | 获取已发布策略 |
| 策略 | POST | /app/{id}/strategy/published/{sid}/disable | 停用策略 |
| 支付 | POST | /pay/order/create | 创建支付订单 |
| 支付 | GET | /pay/order/{order_no} | 获取订单状态 |
| 支付 | POST | /pay/order/query | 查询订单 |
| 授权 | POST | /auth/comprehensive | 用户综合授权 |
| 授权 | GET | /auth/status | 查询授权状态 |
| 授权 | POST | /auth/revoke | 解除授权 |
| 退款 | POST | /pay/refund | 发起退款 |
| 退款 | GET | /pay/refund/{refund_no} | 查询退款状态 |
| 对账 | POST | /reconciliation/trigger | 触发对账 |
| 对账 | GET | /reconciliation/task | 获取任务列表 |
| 对账 | GET | /reconciliation/task/{id} | 获取任务详情 |
| 对账 | GET | /reconciliation/task/{id}/diff | 获取差异明细 |
| 对账 | GET | /reconciliation/task/{id}/report | 获取报告 |
| 回调 | POST | /callback/pay/{appid} | 第三方回调 |
| 管理 | GET | /admin/channel/health | 通道健康状态 |
| 管理 | GET | /admin/trade/stat | 交易统计 |
| 管理 | GET | /admin/settlement/stat | 结算统计 |

---

## 附录

### A. 签名生成示例

```java
// Java 示例
String appid = "app_xxxxxxxx";
String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
String nonce = UUID.randomUUID().toString().replace("-", "");
String body = "{\"amount\":10000}"; // 请求体 JSON 字符串
String secretKey = "***";

String signStr = appid + timestamp + nonce + body + secretKey;
String signature = SHA256.sign(signStr);

// 请求头
httpHeaders.set("X-Appid", appid);
httpHeaders.set("X-Timestamp", timestamp);
httpHeaders.set("X-Nonce", nonce);
httpHeaders.set("X-Signature", signature);
```

### B. 幂等性说明

- 创建订单时，`merchant_order_no` 用作幂等键，同一订单号再次请求返回原结果
- 退款时，`merchant_refund_no` 用作幂等键
- 建议每次请求的 `merchant_order_no` 格式为：`{商户前缀}_{日期}_{序列号}`

### C. 版本升级策略

- API 版本通过 URL 路径区分：`/v1/`、`/v2/` ...
- 老版本在新版本发布后至少保留 6 个月
- 接口变更需在文档中标注 `@deprecated` 及迁移指南

---

*更多细节请参阅：业务流程图.md、用例模型.md、时序图.md、ER模型.md*
