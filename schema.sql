-- ============================================================
-- hioas-upay 支付聚合平台 数据库建表脚本
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS hioas_upay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hioas_upay;

-- ============================================================
-- 商户表
-- ============================================================
CREATE TABLE IF NOT EXISTS merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    external_id VARCHAR(64) COMMENT '外部系统商户标识',
    name VARCHAR(128) NOT NULL COMMENT '商户名称',
    company_name VARCHAR(128) COMMENT '公司全称',
    unified_code VARCHAR(32) COMMENT '统一社会信用代码',
    legal_person VARCHAR(64) COMMENT '法人姓名',
    legal_id_card VARCHAR(32) COMMENT '法人身份证号',
    contact_name VARCHAR(64) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(128) COMMENT '联系邮箱',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待认证、1-认证中、2-已认证、3-暂停、4-封禁',
    cert_status TINYINT DEFAULT 0 COMMENT '认证状态',
    cert_reviewer VARCHAR(64) COMMENT '审核人',
    cert_review_time DATETIME COMMENT '审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_merchant_status (status),
    INDEX idx_merchant_unified_code (unified_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- ============================================================
-- 应用表
-- ============================================================
CREATE TABLE IF NOT EXISTS app (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    merchant_id BIGINT NOT NULL COMMENT '所属商户',
    appid VARCHAR(32) UNIQUE NOT NULL COMMENT '平台分配的唯一应用标识',
    name VARCHAR(128) NOT NULL COMMENT '应用名称',
    type TINYINT NOT NULL COMMENT '应用类型:1-电商、2-服务、3-直播、4-其他',
    callback_url VARCHAR(512) COMMENT '回调地址',
    return_url VARCHAR(512) COMMENT '同步跳转地址',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-草稿、1-启用、2-停用',
    sign_secret_key VARCHAR(128) COMMENT '密钥(加密存储)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_app_merchant_id (merchant_id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

-- ============================================================
-- 支付通道元信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS channel (
    code VARCHAR(32) PRIMARY KEY COMMENT '通道编码: wx_jsapi, alipay_trade, zj_payment, bf_payment',
    name VARCHAR(128) NOT NULL COMMENT '通道名称',
    adapter_class VARCHAR(256) NOT NULL COMMENT '适配器类的全限定名',
    version VARCHAR(16) COMMENT '适配器版本',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-未启用、1-启用、2-维护中',
    scenes JSON COMMENT '支持的场景列表',
    config_template JSON COMMENT '配置模板(字段定义、默认值)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付通道元信息表';

-- 插入默认通道
INSERT INTO channel (code, name, adapter_class, version, status, scenes) VALUES
('wx_jsapi',    '微信支付 JSAPI',         'com.hioas.demo.channel.adapter.WeChatJSAPIAdapter',    '1.0.0', 1, '["ecommerce","app","h5"]'),
('alipay_trade', '支付宝电脑网站支付',     'com.hioas.demo.channel.adapter.AlipayTradeAdapter',     '1.0.0', 1, '["ecommerce","h5"]'),
('zj_payment',   '中金支付',               'com.hioas.demo.channel.adapter.ZjPaymentAdapter',       '1.0.0', 1, '["ecommerce","app"]'),
('bf_payment',   '宝付支付',               'com.hioas.demo.channel.adapter.BfPaymentAdapter',       '1.0.0', 1, '["ecommerce","live"]')
ON DUPLICATE KEY UPDATE name=VALUES(name), adapter_class=VALUES(adapter_class), version=VALUES(version);

-- ============================================================
-- 渠道实例配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS channel_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '所属应用',
    channel_code VARCHAR(32) NOT NULL COMMENT '通道编码',
    instance_name VARCHAR(128) COMMENT '实例名称',
    config JSON COMMENT '通道配置(密钥等,加密存储)',
    fees JSON COMMENT '费率配置',
    amount_limit JSON COMMENT '额度限制',
    priority TINYINT DEFAULT 1 COMMENT '实例优先级',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-未配置、1-已配置、2-测试中、3-正常、4-异常',
    test_result JSON COMMENT '最近测试结果',
    memo VARCHAR(256) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_channel_instance_app_id (app_id),
    INDEX idx_channel_instance_code (channel_code),
    INDEX idx_channel_instance_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道实例配置表';

-- ============================================================
-- 支付策略表
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_strategy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    app_id BIGINT NOT NULL COMMENT '所属应用',
    name VARCHAR(128) NOT NULL COMMENT '策略名称',
    description VARCHAR(512) COMMENT '描述',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-草稿、1-已发布、2-已失效',
    fallback_policy JSON COMMENT '容错策略',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at DATETIME COMMENT '发布时间',
    published_by BIGINT COMMENT '发布人',
    INDEX idx_strategy_app_id (app_id),
    INDEX idx_strategy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付策略表';

-- ============================================================
-- 策略规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS strategy_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    strategy_id BIGINT NOT NULL COMMENT '所属策略',
    priority TINYINT NOT NULL COMMENT '优先级(数字越小越优先)',
    name VARCHAR(128) COMMENT '规则名称',
    description VARCHAR(512) COMMENT '描述',
    `condition` JSON COMMENT '条件表达式',
    channels JSON COMMENT '候选通道列表(channel_code数组)',
    sort_by VARCHAR(32) COMMENT '排序方式:fee_rate_asc,fee_rate_desc,limit_first,latency_asc',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_strategy_id (strategy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='策略规则表';

-- ============================================================
-- 用户授权记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS user_auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '所属应用',
    channel_code VARCHAR(32) NOT NULL COMMENT '通道编码',
    channel_instance_id BIGINT COMMENT '具体的渠道实例',
    auth_type TINYINT NOT NULL DEFAULT 1 COMMENT '授权类型:1-单独授权、2-综合授权',
    auth_status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-未授权、1-已授权、2-授权失败、3-已拒绝、4-授权过期',
    auth_token VARCHAR(512) COMMENT '第三方授权凭证(加密存储)',
    refresh_token VARCHAR(512) COMMENT '刷新token',
    auth_time DATETIME COMMENT '授权时间',
    expires_at DATETIME COMMENT '授权过期时间',
    auth_channel VARCHAR(32) COMMENT '授权来源渠道',
    memo VARCHAR(256) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_auth_user_id (user_id),
    INDEX idx_user_auth_app_id (app_id),
    INDEX idx_user_auth_channel_code (channel_code),
    INDEX idx_user_auth_status (auth_status),
    INDEX idx_user_auth_user_app_channel (user_id, app_id, channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户授权记录表';

-- ============================================================
-- 用户-渠道绑定关系表
-- ============================================================
CREATE TABLE IF NOT EXISTS user_channel_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    channel_code VARCHAR(32) NOT NULL COMMENT '通道编码',
    channel_instance_id BIGINT COMMENT '渠道实例ID',
    bind_time DATETIME COMMENT '绑定时间(首次使用此通道支付成功时)',
    last_used_at DATETIME COMMENT '最近使用时间',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    status TINYINT DEFAULT 1 COMMENT '状态:1-正常、2-停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_binding_user_app (user_id, app_id),
    INDEX idx_binding_channel (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-渠道绑定关系表';

-- ============================================================
-- 交易记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    transaction_no VARCHAR(64) UNIQUE NOT NULL COMMENT '平台交易流水号',
    merchant_order_no VARCHAR(64) NOT NULL COMMENT '商户订单号',
    app_id BIGINT COMMENT '所属应用(订单创建时可能未知,支付执行时补充)',
    user_id BIGINT COMMENT '用户ID',
    channel_code VARCHAR(32) COMMENT '使用的通道编码',
    channel_instance_id BIGINT COMMENT '使用的渠道实例',
    third_order_no VARCHAR(128) COMMENT '第三方订单号',
    order_type TINYINT NOT NULL DEFAULT 1 COMMENT '订单类型:1-支付、2-退款、3-查询、4-撤销',
    amount DECIMAL(16,2) NOT NULL COMMENT '金额(分)',
    currency VARCHAR(8) DEFAULT '' COMMENT '货币代码,默认CNY',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待处理、1-处理中、2-成功、3-失败、4-部分成功、5-已关闭',
    scene VARCHAR(32) COMMENT '支付场景',
    risk_level TINYINT DEFAULT 0 COMMENT '风控等级:0-正常、1-低风险、2-中风险、3-高风险',
    risk_note VARCHAR(256) COMMENT '风控备注',
    metadata JSON COMMENT '扩展字段',
    failure_reason TEXT COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    paid_at DATETIME COMMENT '支付成功时间',
    closed_at DATETIME COMMENT '关闭时间',
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_transaction_app_id (app_id),
    INDEX idx_transaction_user_id (user_id),
    INDEX idx_transaction_channel_code (channel_code),
    INDEX idx_transaction_status (status),
    INDEX idx_transaction_created_at (created_at),
    INDEX idx_transaction_third_order (third_order_no),
    INDEX idx_transaction_merchant_order (merchant_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- ============================================================
-- 退款记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    refund_no VARCHAR(64) UNIQUE NOT NULL COMMENT '退款流水号',
    transaction_id BIGINT NOT NULL COMMENT '关联的原支付交易',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    user_id BIGINT COMMENT '用户ID',
    channel_code VARCHAR(32) COMMENT '使用的退款通道',
    channel_instance_id BIGINT COMMENT '退款使用的实例',
    third_refund_no VARCHAR(128) COMMENT '第三方退款号',
    amount DECIMAL(16,2) NOT NULL COMMENT '退款金额(分)',
    reason VARCHAR(256) COMMENT '退款原因',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待处理、1-处理中、2-成功、3-失败',
    failure_reason TEXT COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    refunded_at DATETIME COMMENT '退款成功时间',
    INDEX idx_refund_no (refund_no),
    INDEX idx_refund_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- ============================================================
-- 回调记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS callback_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    transaction_id BIGINT NOT NULL COMMENT '关联交易',
    callback_url VARCHAR(512) COMMENT '回调目标地址',
    callback_method VARCHAR(16) COMMENT '方法:GET/POST',
    request_params JSON COMMENT '回调请求参数',
    callback_status TINYINT DEFAULT 0 COMMENT '状态:0-待发送、1-发送中、2-成功、3-失败、4-失败待重试',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    last_callback_time DATETIME COMMENT '最近回调时间',
    next_retry_time DATETIME COMMENT '下次重试时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_callback_transaction_id (transaction_id),
    INDEX idx_callback_status (callback_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回调记录表';

-- ============================================================
-- 对账任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS reconciliation_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_no VARCHAR(64) UNIQUE NOT NULL COMMENT '任务编号',
    app_id BIGINT COMMENT '应用ID(为空表示全局对账)',
    channel_code VARCHAR(32) COMMENT '指定通道(为空表示全通道)',
    start_date DATE COMMENT '对账起始日期',
    end_date DATE COMMENT '对账结束日期',
    status TINYINT DEFAULT 0 COMMENT '状态:0-待执行、1-执行中、2-完成、3-部分完成、4-失败',
    total_count INT DEFAULT 0 COMMENT '总交易数',
    success_count INT DEFAULT 0 COMMENT '对账成功数',
    diff_count INT DEFAULT 0 COMMENT '差异数',
    started_at DATETIME COMMENT '开始时间',
    finished_at DATETIME COMMENT '完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recon_task_app_id (app_id),
    INDEX idx_recon_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账任务表';

-- ============================================================
-- 对账明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS reconciliation_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_id BIGINT NOT NULL COMMENT '所属任务',
    transaction_id BIGINT NOT NULL COMMENT '关联交易',
    channel_code VARCHAR(32) COMMENT '通道编码',
    third_order_no VARCHAR(128) COMMENT '第三方订单号',
    third_amount DECIMAL(16,2) COMMENT '第三方金额',
    third_status VARCHAR(32) COMMENT '第三方状态',
    platform_amount DECIMAL(16,2) COMMENT '平台记录金额',
    platform_status VARCHAR(32) COMMENT '平台状态',
    match_result TINYINT DEFAULT 0 COMMENT '匹配结果:0-一致、1-金额不一致、2-状态不一致、3-订单不存在',
    diff_detail TEXT COMMENT '差异详情',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recon_item_task_id (task_id),
    INDEX idx_recon_item_transaction_id (transaction_id),
    INDEX idx_recon_match_result (match_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账明细表';

-- ============================================================
-- 商户白名单表
-- ============================================================
CREATE TABLE IF NOT EXISTS merchant_channel_white_list (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    channel_code VARCHAR(32) NOT NULL COMMENT '通道编码',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    memo VARCHAR(256) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_white_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户白名单表';

-- ============================================================
-- 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    operator_type TINYINT COMMENT '操作类型:1-商户管理员、2-平台管理员',
    operator_id BIGINT COMMENT '操作人ID',
    operation VARCHAR(128) COMMENT '操作名称',
    target_type VARCHAR(32) COMMENT '目标类型',
    target_id VARCHAR(64) COMMENT '目标ID',
    result TINYINT COMMENT '结果:1-成功、2-失败',
    detail TEXT COMMENT '操作详情',
    ip VARCHAR(45) COMMENT '操作IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_op_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 系统配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `key` VARCHAR(128) UNIQUE NOT NULL COMMENT '配置键',
    `value` TEXT COMMENT '配置值',
    description VARCHAR(256) COMMENT '描述',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO system_config (`key`, `value`, description) VALUES
('global.fallback_enabled', 'true', '全局是否启用容错'),
('global.default_max_attempts', '3', '默认最大尝试次数'),
('global.health_check_interval', '30', '通道健康检查间隔(秒)'),
('global.reconciliation.cron', '0 0 2 * * *', '对账定时任务Cron')
ON DUPLICATE KEY UPDATE `value`=VALUES(`value`);
