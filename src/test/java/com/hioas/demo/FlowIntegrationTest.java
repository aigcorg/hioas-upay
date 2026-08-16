package com.hioas.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hioas.demo.entity.Transaction;
import com.hioas.demo.mapper.TransactionMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 支付聚合平台全流程集成测试
 *
 * 按业务顺序依次验证: 商户入驻→应用创建→通道配置→策略发布→支付→授权→退款→对账→取消→管理
 * 每个用例均校验 HTTP 响应字段, 关键流程额外通过 Mapper 校验数据已落库。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    // 每次运行生成唯一后缀, 保证可重复执行不冲突
    private static final String RUN_ID = String.valueOf(System.currentTimeMillis() % 1000000);
    private static final String SUFFIX = RUN_ID;

    // 跨方法共享的业务状态
    private static Long merchantId;
    private static Long appId;
    private static Long mainStrategyId;
    private static Long secondStrategyId;
    private static Long wxInstanceId;
    private static String paidOrderNo;
    private static String paidMerchantOrderNo;
    private static String refundNo;

    private static final String MERCHANT_NAME = "集成测试商户" + SUFFIX;
    private static final String APP_NAME = "集成测试应用" + SUFFIX;
    private static final String PAID_MERCHANT_ORDER = "IT_ORD_" + SUFFIX + "_1";
    private static final String PENDING_MERCHANT_ORDER = "IT_ORD_" + SUFFIX + "_2";
    private static final String REFUND_MERCHANT_NO = "IT_RF_" + SUFFIX;

    // ==================== 工具方法 ====================

    private MvcResult postJson(String url, Object body) throws Exception {
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) req = req.content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(req).andExpect(status().isOk()).andReturn();
    }

    private MvcResult getJson(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpect(status().isOk()).andReturn();
    }

    private MvcResult putJson(String url, Object body) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn();
    }

    private MvcResult deleteJson(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(status().isOk()).andReturn();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode data(JsonNode root) {
        JsonNode d = root.path("data");
        assertFalse(d.isMissingNode() || d.isNull(), "data 节点不应为空: " + root);
        return d;
    }

    private void assertSuccess(JsonNode root) {
        assertEquals("SUCCESS", root.path("code").asText(), "响应码应为 SUCCESS: " + root);
    }

    private Map<String, Object> channelInstanceBody(String code, String name) {
        Map<String, Object> config = new HashMap<>();
        config.put("channelCode", code);
        config.put("instanceName", name);
        Map<String, Object> secret = new HashMap<>();
        secret.put("appid", "IT_" + SUFFIX + "_" + code);
        secret.put("mch_id", "88" + RUN_ID);
        secret.put("secret", "TEST_SECRET_" + SUFFIX);
        config.put("config", secret);
        Map<String, Object> fees = new HashMap<>();
        fees.put("rate", 0.006);
        Map<String, Double> caps = new HashMap<>();
        caps.put("min", 0.01);
        caps.put("max", 10000.0);
        fees.put("caps", caps);
        Map<String, Double> limit = new HashMap<>();
        limit.put("singleMax", 50000.0);
        limit.put("dailyMax", 500000.0);
        config.put("fees", fees);
        config.put("amountLimit", limit);
        return config;
    }

    // ==================== 1. 商户入驻 ====================

    @Test
    @Order(1)
    void merchantRegisterAndCertify() throws Exception {
        // 注册
        Map<String, Object> body = new HashMap<>();
        body.put("name", MERCHANT_NAME);
        body.put("companyName", "集成测试有限公司" + SUFFIX);
        body.put("unifiedCode", "91440300IT" + RUN_ID.substring(Math.max(0, RUN_ID.length() - 4)) + "00");
        body.put("legalPerson", "测试法人");
        body.put("legalIdCard", "44030520000101123" + (RUN_ID.length() > 8 ? RUN_ID.substring(0, 1) : "4"));
        body.put("contactName", "测试联系人");
        body.put("contactPhone", "138" + String.format("%08d", Long.parseLong(SUFFIX) % 100000000));
        body.put("contactEmail", "it" + SUFFIX + "@example.com");

        JsonNode root = json(postJson("/v1/merchant/register", body));
        assertSuccess(root);
        merchantId = data(root).path("id").asLong();
        assertTrue(merchantId > 0, "商户ID应>0");

        // 认证
        Map<String, Object> cert = new HashMap<>();
        cert.put("unifiedCode", body.get("unifiedCode"));
        cert.put("legalPerson", "测试法人");
        cert.put("legalIdCard", body.get("legalIdCard"));
        JsonNode certRoot = json(postJson("/v1/merchant/" + merchantId + "/certification", cert));
        assertSuccess(certRoot);

        // 查询
        JsonNode detail = json(getJson("/v1/merchant/" + merchantId));
        assertSuccess(detail);
        assertEquals(MERCHANT_NAME, data(detail).path("name").asText());

        // 商户列表
        JsonNode list = json(getJson("/v1/merchant"));
        assertSuccess(list);
        JsonNode merchants = data(list).path("merchants");
        assertTrue(merchants.isArray() && merchants.size() >= 1, "商户列表应包含新建商户");
    }

    // ==================== 2. 应用创建/查询/更新 ====================

    @Test
    @Order(2)
    void appCreateReadUpdate() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("merchantId", merchantId);
        body.put("name", APP_NAME);
        body.put("type", 1);
        body.put("callbackUrl", "https://it" + SUFFIX + ".example.com/cb");
        body.put("returnUrl", "https://it" + SUFFIX + ".example.com/return");

        JsonNode root = json(postJson("/v1/app", body));
        assertSuccess(root);
        appId = data(root).path("appId").asLong();
        String appid = data(root).path("appid").asText();
        String secret = data(root).path("signSecretKey").asText();
        assertTrue(appId > 0);
        assertFalse(appid.isEmpty());
        assertFalse(secret.isEmpty(), "应用密钥应已加密返回");

        // 详情
        JsonNode detail = json(getJson("/v1/app/" + appId));
        assertSuccess(detail);
        assertEquals(APP_NAME, data(detail).path("name").asText());

        // 列表
        JsonNode list = json(getJson("/v1/app?merchantId=" + merchantId));
        assertSuccess(list);
        JsonNode apps = data(list).path("apps");
        assertTrue(apps.isArray() && apps.size() >= 1, "应用列表应包含新建应用");

        // 更新
        Map<String, Object> upd = new HashMap<>();
        upd.put("id", appId);
        upd.put("merchantId", merchantId);
        upd.put("appid", appid);
        upd.put("name", APP_NAME + "-已更新");
        upd.put("type", 1);
        upd.put("callbackUrl", body.get("callbackUrl"));
        upd.put("returnUrl", body.get("returnUrl"));
        upd.put("status", 0);
        upd.put("signSecretKey", secret);
        JsonNode updRoot = json(putJson("/v1/app/" + appId, upd));
        assertSuccess(updRoot);
        assertEquals(APP_NAME + "-已更新", data(updRoot).path("name").asText());
    }

    @Test
    @Order(3)
    void appCreateAndDeleteSecond() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("merchantId", merchantId);
        body.put("name", "待删除应用" + SUFFIX);
        body.put("type", 2);
        body.put("callbackUrl", "https://tmp" + SUFFIX + ".example.com/cb");
        body.put("returnUrl", "https://tmp" + SUFFIX + ".example.com/return");

        JsonNode create = json(postJson("/v1/app", body));
        assertSuccess(create);
        long tmpAppId = data(create).path("appId").asLong();
        assertTrue(tmpAppId > 0);

        JsonNode del = json(deleteJson("/v1/app/" + tmpAppId));
        assertSuccess(del);
        assertEquals("删除成功", data(del).asText());
    }

    // ==================== 3. 通道选择/实例配置/测试/删除 ====================

    @Test
    @Order(4)
    void channelSelectAndInstanceConfig() throws Exception {
        // 可用通道
        JsonNode avail = json(getJson("/v1/channel/available"));
        assertSuccess(avail);
        JsonNode channels = data(avail).path("data");
        assertTrue(channels.isArray() && channels.size() >= 4, "应至少4个可用通道");

        // 选择通道
        Map<String, Object> select = new HashMap<>();
        select.put("channelCodes", List.of("wx_jsapi", "alipay_trade"));
        JsonNode selRoot = json(postJson("/v1/channel/app/" + appId + "/select", select));
        assertSuccess(selRoot);

        // 创建生产实例
        JsonNode wx = json(postJson("/v1/channel/app/" + appId + "/instance",
                channelInstanceBody("wx_jsapi", "集成微信实例")));
        assertSuccess(wx);
        wxInstanceId = data(wx).path("id").asLong();
        assertEquals(1, data(wx).path("status").asInt(), "生产实例应启用");

        JsonNode ali = json(postJson("/v1/channel/app/" + appId + "/instance",
                channelInstanceBody("alipay_trade", "集成支付宝实例")));
        assertSuccess(ali);

        // 实例列表
        JsonNode instList = json(getJson("/v1/channel/app/" + appId + "/instance"));
        assertSuccess(instList);
        assertTrue(data(instList).isArray() && data(instList).size() >= 3);
    }

    // ==================== 3b. 通道健康 ====================
    // 注意: 必须在通道实例删除用例之前执行 —— 删除某通道的最后一个实例会注销共享适配器,
    // 导致健康检查少报该通道。故本用例放在 @Order(5), 实例删除用例放在最后 @Order(13)。

    @Test
    @Order(5)
    void channelHealthCheck() throws Exception {
        JsonNode root = json(getJson("/v1/admin/channel/health"));
        assertSuccess(root);
        JsonNode healthList = data(root).path("data");
        assertTrue(healthList.isArray() && healthList.size() >= 4, "应报告全部4个通道");
        for (JsonNode h : healthList) {
            assertEquals("HEALTHY", h.path("status").asText(), "通道应全部健康: " + h);
        }
    }

    @Test
    @Order(13)
    void channelInstanceTestAndDelete() throws Exception {
        // 测试生产实例
        JsonNode test = json(postJson("/v1/channel/app/" + appId + "/instance/" + wxInstanceId + "/test", null));
        assertSuccess(test);
        assertEquals("CONNECTED", data(test).path("status").asText(), "实例测试应连接成功");

        // 创建临时实例 -> 测试 -> 删除
        JsonNode tmp = json(postJson("/v1/channel/app/" + appId + "/instance",
                channelInstanceBody("zj_payment", "临时中金实例")));
        assertSuccess(tmp);
        long tmpId = data(tmp).path("id").asLong();
        assertTrue(tmpId > 0);

        JsonNode tmpTest = json(postJson("/v1/channel/app/" + appId + "/instance/" + tmpId + "/test", null));
        assertSuccess(tmpTest);
        assertEquals("CONNECTED", data(tmpTest).path("status").asText());

        JsonNode del = json(deleteJson("/v1/channel/app/" + appId + "/instance/" + tmpId));
        assertSuccess(del);
        assertEquals("删除成功", data(del).asText());

        // 删除后列表不应包含临时实例
        JsonNode instList = json(getJson("/v1/channel/app/" + appId + "/instance"));
        assertSuccess(instList);
        boolean tmpGone = true;
        for (JsonNode n : data(instList)) {
            if (n.path("id").asLong() == tmpId) tmpGone = false;
        }
        assertTrue(tmpGone, "临时实例应已删除");
    }

    // ==================== 4. 策略: 草稿/发布 ====================

    @Test
    @Order(6)
    void strategyDraftAndPublish() throws Exception {
        Map<String, Object> ruleCond = new HashMap<>();
        ruleCond.put("amountRange", List.of(0.01, 10000.0));
        ruleCond.put("scene", "ecommerce");
        ruleCond.put("riskLevel", 0);
        Map<String, Object> rule = new HashMap<>();
        rule.put("priority", 1);
        rule.put("name", "主规则");
        rule.put("condition", ruleCond);
        rule.put("channels", List.of("wx_jsapi", "alipay_trade"));
        rule.put("sortBy", "fee_rate_asc");

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("enabled", true);
        fallback.put("maxAttempts", 3);
        fallback.put("requireReAuthorization", false);

        Map<String, Object> draft = new HashMap<>();
        draft.put("name", "主策略" + SUFFIX);
        draft.put("description", "集成测试主策略");
        draft.put("rules", List.of(rule));
        draft.put("fallbackPolicy", fallback);

        JsonNode create = json(postJson("/v1/strategy/app/" + appId + "/draft", draft));
        assertSuccess(create);
        mainStrategyId = data(create).path("strategyId").asLong();
        assertEquals(0, data(create).path("status").asInt(), "草稿状态应为0");

        // 草稿列表
        JsonNode draftList = json(getJson("/v1/strategy/app/" + appId + "/draft"));
        assertSuccess(draftList);
        assertTrue(data(draftList).size() >= 1);

        // 草稿详情
        JsonNode detail = json(getJson("/v1/strategy/draft/" + mainStrategyId));
        assertSuccess(detail);
        assertEquals(mainStrategyId, data(detail).path("id").asLong());

        // 发布
        JsonNode pub = json(postJson("/v1/strategy/draft/" + mainStrategyId + "/publish", null));
        assertSuccess(pub);
        assertEquals(1, data(pub).path("status").asInt(), "发布后状态应为1");

        // 已发布列表
        JsonNode pubList = json(getJson("/v1/strategy/app/" + appId + "/published"));
        assertSuccess(pubList);
        assertEquals(mainStrategyId, data(pubList).get(0).path("id").asLong(), "主策略应已发布");
    }

    @Test
    @Order(7)
    void strategySecondDraftUpdatePublishDisableDelete() throws Exception {
        // 建第二个草稿
        Map<String, Object> ruleCond = new HashMap<>();
        ruleCond.put("amountRange", List.of(0.01, 100000.0));
        ruleCond.put("scene", "ecommerce");
        ruleCond.put("riskLevel", 0);
        Map<String, Object> rule = new HashMap<>();
        rule.put("priority", 1);
        rule.put("name", "备用规则");
        rule.put("condition", ruleCond);
        rule.put("channels", List.of("wx_jsapi", "alipay_trade"));
        rule.put("sortBy", "fee_rate_asc");
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("enabled", true);
        fallback.put("maxAttempts", 2);
        fallback.put("requireReAuthorization", false);
        Map<String, Object> draft = new HashMap<>();
        draft.put("name", "备用策略" + SUFFIX);
        draft.put("description", "集成测试备用策略");
        draft.put("rules", List.of(rule));
        draft.put("fallbackPolicy", fallback);

        JsonNode create = json(postJson("/v1/strategy/app/" + appId + "/draft", draft));
        assertSuccess(create);
        secondStrategyId = data(create).path("strategyId").asLong();

        // 更新草稿
        Map<String, Object> upd = new HashMap<>();
        upd.put("id", secondStrategyId);
        upd.put("appId", appId);
        upd.put("name", "备用策略" + SUFFIX + "-更新");
        upd.put("description", "更新后描述");
        upd.put("version", 1);
        upd.put("status", 0);
        upd.put("fallbackPolicy", objectMapper.writeValueAsString(fallback));
        JsonNode updRoot = json(putJson("/v1/strategy/draft/" + secondStrategyId, upd));
        assertSuccess(updRoot);
        assertEquals(2, data(updRoot).path("version").asInt(), "更新后版本号应+1");
        assertEquals("备用策略" + SUFFIX + "-更新", data(updRoot).path("name").asText());

        // 发布(自动停用旧策略)
        JsonNode pub = json(postJson("/v1/strategy/draft/" + secondStrategyId + "/publish", null));
        assertSuccess(pub);
        assertEquals(1, data(pub).path("status").asInt());

        // 停用
        JsonNode dis = json(postJson("/v1/strategy/published/" + secondStrategyId + "/disable", null));
        assertSuccess(dis);

        // 已发布列表不应包含备用策略
        JsonNode pubList = json(getJson("/v1/strategy/app/" + appId + "/published"));
        assertSuccess(pubList);
        for (JsonNode n : data(pubList)) {
            assertNotEquals(secondStrategyId, n.path("id").asLong(), "备用策略应已停用");
        }

        // 重新发布主策略恢复活跃
        JsonNode repub = json(postJson("/v1/strategy/draft/" + mainStrategyId + "/publish", null));
        assertSuccess(repub);
        JsonNode finalList = json(getJson("/v1/strategy/app/" + appId + "/published"));
        assertSuccess(finalList);
        assertEquals(mainStrategyId, data(finalList).get(0).path("id").asLong(), "主策略应恢复为活跃");

        // 删除备用策略
        JsonNode del = json(deleteJson("/v1/strategy/draft/" + secondStrategyId));
        assertSuccess(del);
        assertEquals("删除成功", data(del).asText());

        // 草稿列表不应包含已删除的备用策略
        JsonNode draftList = json(getJson("/v1/strategy/app/" + appId + "/draft"));
        assertSuccess(draftList);
        for (JsonNode n : data(draftList)) {
            assertNotEquals(secondStrategyId, n.path("id").asLong(), "备用策略应已删除");
        }
    }

    // ==================== 5. 支付: 下单/路由/授权/执行 ====================

    @Test
    @Order(8)
    void paymentCreateExecuteAuthQuery() throws Exception {
        // 创建订单
        Map<String, Object> order = new HashMap<>();
        order.put("merchantOrderNo", PAID_MERCHANT_ORDER);
        order.put("amount", 88.50);
        order.put("currency", "CNY");
        order.put("scene", "ecommerce");
        order.put("riskLevel", 0);
        order.put("productName", "集成测试商品");
        order.put("productDesc", "全流程支付测试");
        Map<String, Object> cb = new HashMap<>();
        cb.put("notify_url", "https://it" + SUFFIX + ".example.com/notify");
        order.put("callbackParams", cb);

        JsonNode created = json(postJson("/v1/pay/order/create", order));
        assertSuccess(created);
        paidOrderNo = data(created).path("orderNo").asText();
        assertEquals("PENDING", data(created).path("status").asText());

        // 按单号查询订单
        JsonNode getOrder = json(getJson("/v1/pay/order/" + paidOrderNo));
        assertSuccess(getOrder);
        assertEquals(paidOrderNo, data(getOrder).path("orderNo").asText());

        // 首次执行 -> 需要授权
        Map<String, Object> exec = new HashMap<>();
        exec.put("appId", appId);
        exec.put("userId", 10001L);
        exec.put("merchantOrderNo", PAID_MERCHANT_ORDER);
        exec.put("amount", 88.50);
        exec.put("currency", "CNY");
        exec.put("scene", "ecommerce");
        exec.put("riskLevel", 0);

        JsonNode first = json(postJson("/v1/pay/order/execute", exec));
        assertSuccess(first);
        assertEquals("NEED_AUTH", data(first).path("code").asText(), "首次执行应要求授权");

        // 综合授权
        Map<String, Object> authReq = new HashMap<>();
        authReq.put("appId", appId);
        authReq.put("userId", 10001L);
        authReq.put("channels", List.of("wx_jsapi", "alipay_trade"));
        JsonNode auth = json(postJson("/v1/auth/comprehensive", authReq));
        assertSuccess(auth);
        assertEquals("AUTHORIZED", data(auth).path("authResult").path("wx_jsapi").asText());
        assertTrue(data(auth).path("allAuthorized").asBoolean());

        // 授权状态
        JsonNode status = json(getJson("/v1/auth/status?appId=" + appId + "&userId=10001"));
        assertSuccess(status);
        assertEquals("AUTHORIZED", data(status).path("authStatus").path("wx_jsapi").asText());

        // 二次执行 -> 支付成功
        JsonNode second = json(postJson("/v1/pay/order/execute", exec));
        assertSuccess(second);
        assertEquals("SUCCESS", data(second).path("code").asText(), "二次执行应支付成功");
        assertEquals("wx_jsapi", data(second).path("channelCode").asText(), "路由应选择微信");
        assertFalse(data(second).path("thirdOrderNo").asText().isEmpty());

        // 订单状态查询
        JsonNode queryReq = new ObjectMapper().createObjectNode().put("orderNo", paidOrderNo);
        JsonNode q = json(postJson("/v1/pay/order/query", queryReq));
        assertSuccess(q);
        assertEquals("SUCCESS", data(q).path("status").asText(), "支付后订单应为SUCCESS");
        assertFalse(data(q).path("paidAt").asText().isEmpty(), "支付时间应已写入");

        // 校验DB: 交易落库且携带应用上下文
        Transaction tx = transactionMapper.selectByTransactionNo(paidOrderNo);
        assertNotNull(tx, "交易应落库");
        assertEquals(2, tx.getStatus(), "交易状态应为SUCCESS");
        assertEquals(appId, tx.getAppId(), "交易应携带appId");
        assertEquals("wx_jsapi", tx.getChannelCode());
        assertNotNull(tx.getThirdOrderNo());
        assertNotNull(tx.getPaidAt());
    }

    @Test
    @Order(9)
    void authRevokeAndRestore() throws Exception {
        // 解除微信授权
        Map<String, Object> revoke = new HashMap<>();
        revoke.put("appId", appId);
        revoke.put("userId", 10001L);
        revoke.put("channelCode", "wx_jsapi");
        JsonNode revRoot = json(postJson("/v1/auth/revoke", revoke));
        assertSuccess(revRoot);

        JsonNode status = json(getJson("/v1/auth/status?appId=" + appId + "&userId=10001"));
        assertSuccess(status);
        assertEquals("NOT_AUTHORIZED", data(status).path("authStatus").path("wx_jsapi").asText(), "解除后应为未授权");

        // 重新授权恢复
        Map<String, Object> authReq = new HashMap<>();
        authReq.put("appId", appId);
        authReq.put("userId", 10001L);
        authReq.put("channels", List.of("wx_jsapi", "alipay_trade"));
        JsonNode auth = json(postJson("/v1/auth/comprehensive", authReq));
        assertSuccess(auth);
        assertTrue(data(auth).path("allAuthorized").asBoolean());

        JsonNode restored = json(getJson("/v1/auth/status?appId=" + appId + "&userId=10001"));
        assertSuccess(restored);
        assertEquals("AUTHORIZED", data(restored).path("authStatus").path("wx_jsapi").asText(), "应已恢复授权");
    }

    // ==================== 6. 退款 ====================

    @Test
    @Order(10)
    void refundFlow() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("merchantRefundNo", REFUND_MERCHANT_NO);
        req.put("orderNo", paidOrderNo);
        req.put("amount", 10.50);
        req.put("reason", "集成测试退款");
        Map<String, Object> cb = new HashMap<>();
        cb.put("notify_url", "https://it" + SUFFIX + ".example.com/refund/notify");
        req.put("callbackParams", cb);

        JsonNode root = json(postJson("/v1/pay/refund", req));
        assertSuccess(root);
        refundNo = data(root).path("refundNo").asText();
        assertEquals("SUCCESS", data(root).path("status").asText(), "退款应成功");
        assertEquals("wx_jsapi", data(root).path("channelCode").asText());
        assertFalse(data(root).path("thirdRefundNo").asText().isEmpty());

        // 退款查询: orderNo 应为交易单号
        JsonNode detail = json(getJson("/v1/pay/refund/" + refundNo));
        assertSuccess(detail);
        assertEquals(paidOrderNo, data(detail).path("orderNo").asText(), "退款查询orderNo应回显交易单号");
        assertEquals("SUCCESS", data(detail).path("status").asText());
    }

    // ==================== 7. 对账 ====================

    @Test
    @Order(11)
    void reconciliationFlow() throws Exception {
        String today = LocalDate.now().toString();
        Map<String, Object> req = new HashMap<>();
        req.put("appId", appId);
        req.put("channelCode", "wx_jsapi");
        req.put("startDate", today);
        req.put("endDate", today);

        JsonNode trigger = json(postJson("/v1/reconciliation/trigger", req));
        assertSuccess(trigger);
        long taskId = data(trigger).path("taskId").asLong();
        assertTrue(taskId > 0);
        assertEquals(2, data(trigger).path("status").asInt(), "对账应同步完成");

        // 任务列表
        JsonNode taskList = json(getJson("/v1/reconciliation/task?appId=" + appId));
        assertSuccess(taskList);
        assertTrue(data(taskList).size() >= 1);

        // 任务详情
        JsonNode detail = json(getJson("/v1/reconciliation/task/" + taskId));
        assertSuccess(detail);
        assertEquals("COMPLETED", data(detail).path("status").asText());
        assertTrue(data(detail).path("totalCount").asInt() >= 1, "应至少对账1笔交易");
        assertEquals(0, data(detail).path("diffCount").asInt(), "模拟通道下应无差异");

        // 差异列表
        JsonNode diff = json(getJson("/v1/reconciliation/task/" + taskId + "/diff"));
        assertSuccess(diff);
        assertEquals(0, data(diff).size(), "无差异");

        // 报告应包含全部明细
        JsonNode report = json(getJson("/v1/reconciliation/task/" + taskId + "/report"));
        assertSuccess(report);
        String csv = data(report).asText();
        assertTrue(csv.contains("CONSISTENT"), "报告应包含一致项: " + csv);
    }

    // ==================== 8. 订单取消 ====================

    @Test
    @Order(12)
    void orderCancelFlow() throws Exception {
        // 待支付订单取消 -> 应失败(未路由通道)
        Map<String, Object> order = new HashMap<>();
        order.put("merchantOrderNo", PENDING_MERCHANT_ORDER);
        order.put("amount", 99.00);
        order.put("currency", "CNY");
        order.put("scene", "ecommerce");
        order.put("riskLevel", 0);
        order.put("productName", "待取消订单");
        order.put("productDesc", "订单取消测试");
        JsonNode created = json(postJson("/v1/pay/order/create", order));
        assertSuccess(created);
        String pendingNo = data(created).path("orderNo").asText();

        JsonNode cancelPending = json(postJson("/v1/pay/order/" + pendingNo + "/cancel", null));
        assertEquals("CANCEL_FAILED", cancelPending.path("code").asText(), "未支付订单应无法取消");
        assertFalse(cancelPending.path("message").asText().isEmpty());

        // 已支付订单取消 -> 应成功
        JsonNode cancelPaid = json(postJson("/v1/pay/order/" + paidOrderNo + "/cancel", null));
        assertSuccess(cancelPaid);
        assertEquals("取消成功", data(cancelPaid).asText());
    }
}
