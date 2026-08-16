<template>
  <div class="dashboard">
    <!-- ===== 顶部导航 ===== -->
    <header class="topbar">
      <div class="topbar__left">
        <a class="brand-logo" href="#">
          <span class="brand-logo__icon"><i class="ri-wallet-3-line"></i></span>
          <span class="brand-logo__name">PayHub</span>
        </a>

        <button type="button" class="app-switcher" @click="onSwitchApp">
          <span class="app-switcher__name">电商主站支付</span>
          <i class="ri-arrow-down-s-line"></i>
        </button>
      </div>

      <div class="topbar__right">
        <button type="button" class="icon-btn" title="通知" @click="onNotify">
          <i class="ri-notification-3-line"></i>
          <span class="icon-btn__dot"></span>
        </button>

        <div class="user-chip">
          <span class="user-chip__avatar">张</span>
          <span class="user-chip__name">张经理</span>
          <i class="ri-arrow-down-s-line"></i>
        </div>
      </div>
    </header>

    <div class="dashboard-body">
      <!-- ===== 左侧导航 ===== -->
      <aside class="sidebar">
        <nav class="side-menu">
          <button
            v-for="item in menus"
            :key="item.key"
            type="button"
            class="side-menu__item"
            :class="{ 'side-menu__item--active': item.active }"
            @click="selectMenu(item)"
          >
            <i class="side-menu__icon" :class="item.icon"></i>
            <span class="side-menu__label">{{ item.label }}</span>
          </button>
        </nav>

        <div class="help-card">
          <i class="ri-question-line help-card__icon"></i>
          <p class="help-card__title">需要帮助？</p>
          <p class="help-card__desc">查看文档或联系客服</p>
          <button type="button" class="help-card__link" @click="onHelp">
            查看帮助中心 <i class="ri-arrow-right-s-line"></i>
          </button>
        </div>
      </aside>

      <!-- ===== 主内容区 ===== -->
      <main class="main">
        <div class="page-header">
          <div class="page-header__titles">
            <h1 class="page-title">仪表板</h1>
            <p class="page-subtitle">欢迎回来，张经理！以下是您的支付应用概览</p>
          </div>
          <button type="button" class="create-app-btn" @click="onCreateApp">
            <i class="ri-add-line"></i>
            创建应用
          </button>
        </div>

        <!-- 统计卡片区 -->
        <section class="stat-grid">
          <div v-for="card in statCards" :key="card.label" class="stat-card">
            <div class="stat-card__header">
              <span class="stat-card__label">{{ card.label }}</span>
              <span class="stat-card__icon" :style="{ background: card.iconBg, color: card.iconColor }">
                <i :class="card.icon"></i>
              </span>
            </div>
            <p class="stat-card__value">{{ card.value }}</p>
            <div v-if="card.type === 'health'" class="health-indicator">
              <span class="health-indicator__dot" style="background: #2e7d32"></span>
              <span class="health-indicator__ok">3 个正常</span>
              <span class="health-indicator__dot" style="background: #c62828"></span>
              <span class="health-indicator__bad">1 个异常</span>
            </div>
            <div v-else class="stat-card__trend">
              <i class="ri-arrow-up-line" :style="{ color: card.trendColor }"></i>
              <span class="stat-card__trend-value" :style="{ color: card.trendColor }">{{ card.trend }}</span>
              <span class="stat-card__trend-note">{{ card.note }}</span>
            </div>
          </div>
        </section>

        <!-- 应用列表区 -->
        <section class="app-section">
          <div class="app-section__header">
            <h2 class="app-section__title">我的应用</h2>
            <button type="button" class="view-all" @click="onViewAll">
              查看全部 <i class="ri-arrow-right-s-line"></i>
            </button>
          </div>

          <div class="app-grid">
            <div v-for="app in apps" :key="app.appId" class="app-card">
              <div class="app-card__head">
                <span class="app-card__icon" :style="{ background: app.iconBg, color: app.iconColor }">
                  <i :class="app.icon"></i>
                </span>
                <span class="app-card__badge" :style="{ background: app.statusBg, color: app.statusColor }">
                  {{ app.status }}
                </span>
              </div>
              <p class="app-card__name">{{ app.name }}</p>
              <p class="app-card__id">{{ app.appId }}</p>
              <ul class="app-card__info">
                <li><i class="ri-plug-line"></i>{{ app.channels }}</li>
                <li><i class="ri-route-line"></i>{{ app.strategy }}</li>
                <li><i class="ri-time-line"></i>{{ app.created }}</li>
              </ul>
              <div class="app-card__actions">
                <button
                  type="button"
                  class="btn-primary"
                  :class="{ 'btn-primary--filled': app.filled }"
                  @click="onAppAction(app)"
                >
                  {{ app.action }}
                </button>
                <button type="button" class="btn-secondary" @click="onAppDetail(app)">详情</button>
              </div>
            </div>

            <!-- 新建应用卡片 -->
            <button type="button" class="app-card app-card--new" @click="onCreateApp">
              <span class="app-card__plus"><i class="ri-add-line"></i></span>
              <span class="app-card__new-label">创建新应用</span>
            </button>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

/* ===== 左侧导航菜单 ===== */
const menus = [
  { key: 'dashboard', label: '仪表板', icon: 'ri-dashboard-line', active: true },
  { key: 'apps', label: '应用管理', icon: 'ri-apps-2-line' },
  { key: 'channels', label: '通道管理', icon: 'ri-plug-line' },
  { key: 'strategy', label: '策略配置', icon: 'ri-route-line' },
  { key: 'transactions', label: '交易记录', icon: 'ri-file-list-3-line' },
  { key: 'reconcile', label: '对账管理', icon: 'ri-exchange-funds-line' },
  { key: 'analytics', label: '统计分析', icon: 'ri-bar-chart-box-line' },
]
const activeMenu = ref('dashboard')

function selectMenu(item) {
  activeMenu.value = item.key
  menus.forEach((m) => (m.active = m.key === item.key))
}

/* ===== 统计卡片 ===== */
const statCards = [
  {
    label: '今日交易额', icon: 'ri-money-cny-circle-line',
    iconBg: '#E8F5E9', iconColor: '#2E7D32',
    value: '¥ 128,450.00', trend: '+12.5%', trendColor: '#2E7D32', note: '较昨日',
  },
  {
    label: '今日交易笔数', icon: 'ri-file-list-3-line',
    iconBg: '#E3F2FD', iconColor: '#2B5EA7',
    value: '1,286', trend: '+8.2%', trendColor: '#2E7D32', note: '较昨日',
  },
  {
    label: '支付成功率', icon: 'ri-check-double-line',
    iconBg: '#FFF3E0', iconColor: '#EF6C00',
    value: '98.7%', trend: '+0.3%', trendColor: '#2E7D32', note: '较昨日',
  },
  {
    label: '通道健康', icon: 'ri-heart-pulse-line',
    iconBg: '#FFEBEE', iconColor: '#C62828',
    value: '3 / 4', type: 'health',
  },
]

/* ===== 应用列表 ===== */
const apps = [
  {
    icon: 'ri-shopping-bag-3-line', iconBg: '#E8F0FE', iconColor: '#1A3B6B',
    status: '已启用', statusBg: '#E8F5E9', statusColor: '#2E7D32',
    name: '电商主站支付', appId: 'app_16280123_a7f3',
    channels: '已接入 4 个通道', strategy: '策略：电商默认策略', created: '创建于 2026-08-10',
    action: '管理', filled: true,
  },
  {
    icon: 'ri-live-line', iconBg: '#FFF3E0', iconColor: '#EF6C00',
    status: '草稿', statusBg: '#FFF3E0', statusColor: '#EF6C00',
    name: '直播打赏支付', appId: 'app_16280145_b2c9',
    channels: '已接入 2 个通道', strategy: '策略：未配置', created: '创建于 2026-08-14',
    action: '继续配置', filled: true,
  },
]

/* ===== 操作（待接入真实路由/接口） ===== */
function onSwitchApp() { /* TODO: 应用切换下拉 */ }
function onNotify() { /* TODO: 通知中心 */ }
function onHelp() { /* TODO: 帮助中心 */ }
function onCreateApp() { /* TODO: 创建应用流程 */ }
function onViewAll() { /* TODO: 应用列表页 */ }
function onAppAction(app) { /* TODO: 管理 / 继续配置 */ }
function onAppDetail(app) { /* TODO: 应用详情 */ }
</script>

<style scoped>
/* ===== 页面框架 ===== */
.dashboard {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.topbar {
  flex: none;
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 41px 0 32px;
}

.topbar__left,
.topbar__right {
  display: flex;
  align-items: center;
}

.brand-logo {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.brand-logo__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #1a3b6b;
  color: #fff;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo__name {
  margin-left: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1a3b6b;
}

.app-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 40px;
  border: none;
  background: #f5f5f5;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  font-family: inherit;
}

.app-switcher i {
  font-size: 16px;
  color: #666;
}

.topbar__right {
  gap: 20px;
}

.icon-btn {
  position: relative;
  border: none;
  background: transparent;
  font-size: 20px;
  color: #666;
  cursor: pointer;
  display: flex;
  padding: 2px;
}

.icon-btn__dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 9px;
  height: 8px;
  border-radius: 4px;
  background: #c62828;
}

.user-chip {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-chip__avatar {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  background: #2b5ea7;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-chip__name {
  margin: 0 4px 0 10px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.user-chip i {
  font-size: 16px;
  color: #666;
}

/* ===== 主体 ===== */
.dashboard-body {
  flex: 1;
  min-height: 0;
  display: flex;
}

/* ===== 左侧导航 ===== */
.sidebar {
  flex: none;
  width: 220px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding: 24px 0;
}

.side-menu {
  display: flex;
  flex-direction: column;
}

.side-menu__item {
  display: flex;
  align-items: center;
  height: 51px;
  width: 100%;
  padding: 0 24px;
  border: none;
  border-left: 3px solid transparent;
  background: transparent;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: background 0.15s;
}

.side-menu__item:hover {
  background: #f7f9fc;
}

.side-menu__item--active {
  border: 3px solid #1a3b6b;
  background: #e8f0fe;
  color: #1a3b6b;
  font-weight: 600;
}

.side-menu__icon {
  font-size: 18px;
  margin-right: 12px;
}

.side-menu__item--active .side-menu__icon {
  color: #1a3b6b;
}

.help-card {
  margin: 25px 24px 0;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.help-card__icon {
  display: block;
  font-size: 20px;
  color: #2b5ea7;
}

.help-card__title {
  margin-top: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.help-card__desc {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.help-card__link {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  border: none;
  background: transparent;
  font-size: 12px;
  font-weight: 500;
  color: #2b5ea7;
  cursor: pointer;
  font-family: inherit;
  padding: 0;
}

.help-card__link i {
  font-size: 14px;
}

/* ===== 主内容区 ===== */
.main {
  flex: 1;
  min-width: 0;
  padding: 32px;
  overflow-y: auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a3b6b;
}

.page-subtitle {
  margin-top: 4px;
  font-size: 14px;
  color: #666;
}

.create-app-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: #1a3b6b;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  border-radius: 6px;
  padding: 10px 20px;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}

.create-app-btn:hover {
  background: #234a82;
}

.create-app-btn i {
  font-size: 16px;
}

/* ===== 统计卡片 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 24px;
  min-height: 171px;
}

.stat-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-card__label {
  font-size: 14px;
  color: #666;
}

.stat-card__icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.stat-card__value {
  margin-top: 16px;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-card__trend {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.stat-card__trend i {
  font-size: 14px;
  margin-right: 6px;
}

.stat-card__trend-value {
  font-size: 12px;
  font-weight: 500;
  margin-right: 6px;
}

.stat-card__trend-note {
  font-size: 12px;
  color: #999;
}

.health-indicator {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.health-indicator__dot {
  width: 9px;
  height: 8px;
  border-radius: 4px;
  margin-right: 6px;
}

.health-indicator__ok,
.health-indicator__bad {
  font-size: 12px;
  font-weight: 500;
  margin-right: 14px;
}

.health-indicator__ok {
  color: #2e7d32;
}

.health-indicator__bad {
  color: #c62828;
}

/* ===== 应用列表 ===== */
.app-section {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 24px;
  margin-top: 32px;
}

.app-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.app-section__title {
  font-size: 18px;
  font-weight: 600;
  color: #1a3b6b;
}

.view-all {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #2b5ea7;
  cursor: pointer;
  font-family: inherit;
  padding: 0;
}

.view-all i {
  font-size: 16px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.app-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  background: #fff;
  min-height: 314px;
}

.app-card__head {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.app-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.app-card__badge {
  font-size: 12px;
  font-weight: 500;
  border-radius: 12px;
  padding: 4px 10px;
}

.app-card__name {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.app-card__id {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.app-card__info {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.app-card__info li {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.app-card__info i {
  font-size: 14px;
  color: #999;
  margin-right: 8px;
}

.app-card__actions {
  width: 100%;
  display: flex;
  gap: 8px;
  margin-top: auto;
  padding-top: 16px;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  height: 39px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}

.btn-primary--filled {
  border: none;
  background: #1a3b6b;
  color: #fff;
}

.btn-primary--filled:hover {
  background: #234a82;
}

.btn-secondary {
  border: 1px solid #e0e0e0;
  background: #fff;
  color: #666;
}

.btn-secondary:hover {
  background: #f7f9fc;
}

/* 新建应用卡片 */
.app-card--new {
  border: 2px dashed #e0e0e0;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.app-card--new:hover {
  border-color: #2b5ea7;
  background: #fafcff;
}

.app-card__plus {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #999;
}

.app-card__new-label {
  margin-top: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
}
</style>
