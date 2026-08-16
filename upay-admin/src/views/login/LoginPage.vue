<template>
  <div class="login-page">
    <div class="login-layout">
      <!-- ===== 左侧品牌区 (792px / #1A3B6B) ===== -->
      <aside class="brand-panel">
        <!-- 品牌 Logo -->
        <div class="brand-logo">
          <span class="brand-logo__icon"><i class="ri-wallet-3-line"></i></span>
          <span class="brand-logo__name">PayHub</span>
        </div>

        <!-- 品牌标语 + 特性列表 -->
        <div class="brand-slogan">
          <h1 class="brand-slogan__title">多通道支付聚合平台</h1>
          <p class="brand-slogan__subtitle">一个接口接入所有支付通道，智能路由，无感容错</p>

          <ul class="feature-list">
            <li class="feature-item" v-for="feat in features" :key="feat.icon">
              <span class="feature-item__icon"><i :class="feat.icon"></i></span>
              <span class="feature-item__text">{{ feat.text }}</span>
            </li>
          </ul>
        </div>

        <!-- 底部版权 -->
        <div class="brand-copyright">
          <i class="ri-copyright-line"></i>
          <span>2026 PayHub. All rights reserved.</span>
        </div>
      </aside>

      <!-- ===== 右侧登录区 (648px) ===== -->
      <main class="login-panel">
        <div class="login-card">
          <div class="login-card__header">
            <h2 class="login-card__title">欢迎回来</h2>
            <p class="login-card__subtitle">登录您的 PayHub 商户账户</p>
          </div>

          <!-- 登录方式切换 -->
          <div class="login-tabs" role="tablist">
            <button
              type="button"
              class="login-tab"
              :class="{ 'login-tab--active': loginMode === 'phone' }"
              :aria-selected="loginMode === 'phone'"
              role="tab"
              @click="loginMode = 'phone'"
            >
              手机号登录
            </button>
            <button
              type="button"
              class="login-tab"
              :class="{ 'login-tab--active': loginMode === 'email' }"
              :aria-selected="loginMode === 'email'"
              role="tab"
              @click="loginMode = 'email'"
            >
              邮箱登录
            </button>
          </div>

          <form class="login-form" @submit.prevent="handleLogin" novalidate>
            <!-- 手机号 / 邮箱输入区 -->
            <div class="form-field">
              <label class="form-field__label" :for="loginMode === 'phone' ? 'phone' : 'email'">
                {{ loginMode === 'phone' ? '手机号' : '邮箱' }}
              </label>
              <div class="form-input">
                <i
                  class="form-input__icon"
                  :class="loginMode === 'phone' ? 'ri-smartphone-line' : 'ri-mail-line'"
                ></i>
                <input
                  :id="loginMode === 'phone' ? 'phone' : 'email'"
                  v-model.trim="account"
                  :type="loginMode === 'phone' ? 'tel' : 'email'"
                  :placeholder="loginMode === 'phone' ? '请输入手机号' : '请输入邮箱'"
                  autocomplete="username"
                />
              </div>
            </div>

            <!-- 密码输入区 -->
            <div class="form-field form-field--password">
              <div class="form-field__label-row">
                <label class="form-field__label" for="password">密码</label>
                <a class="form-field__link" href="javascript:;" @click.prevent="onForgotPassword">
                  忘记密码？
                </a>
              </div>
              <div class="form-input">
                <i class="form-input__icon ri-lock-line"></i>
                <input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="form-input__eye"
                  :title="showPassword ? '隐藏密码' : '显示密码'"
                  @click="showPassword = !showPassword"
                >
                  <i :class="showPassword ? 'ri-eye-line' : 'ri-eye-off-line'"></i>
                </button>
              </div>
            </div>

            <!-- 记住我 -->
            <div class="login-options">
              <label class="remember-me">
                <input type="checkbox" v-model="remember" class="remember-me__checkbox" />
                <span class="remember-me__label">记住我</span>
              </label>
            </div>

            <button type="submit" class="login-button" :disabled="submitting">
              {{ submitting ? '登录中…' : '登 录' }}
            </button>

            <p v-if="errorMessage" class="login-error">{{ errorMessage }}</p>
          </form>

          <!-- 注册引导 -->
          <div class="login-register">
            <span class="login-register__text">还没有账户？</span>
            <a class="login-register__link" href="javascript:;" @click.prevent="onRegister">
              立即注册
            </a>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

/* ===== 左侧品牌区数据（来源：Calicat 设计稿） ===== */
const features = [
  { icon: 'ri-plug-line', text: '统一 API 接入微信、支付宝、中金、宝付等通道' },
  { icon: 'ri-route-line', text: '智能策略路由，按费率、额度、场景动态选通道' },
  { icon: 'ri-shield-check-line', text: '通道异常自动无感切换，用户零感知' },
  { icon: 'ri-settings-3-line', text: '新通道配置化接入，无需代码开发' },
]

/* ===== 登录表单状态 ===== */
const loginMode = ref('phone')
const account = ref('')
const password = ref('')
const remember = ref(true)
const showPassword = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

function validate() {
  const isPhone = loginMode.value === 'phone'
  if (!account.value) return isPhone ? '请输入手机号' : '请输入邮箱'
  if (isPhone && !/^1[3-9]\d{9}$/.test(account.value)) return '手机号格式不正确'
  if (!isPhone && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(account.value)) return '邮箱格式不正确'
  if (!password.value) return '请输入密码'
  if (password.value.length < 6) return '密码至少 6 位'
  return ''
}

async function handleLogin() {
  errorMessage.value = ''
  const err = validate()
  if (err) {
    errorMessage.value = err
    return
  }

  submitting.value = true
  // TODO: 对接后端登录接口（hioas-upay 服务端），成功后跳转工作台
  await new Promise((resolve) => setTimeout(resolve, 800))
  submitting.value = false
  router.push('/dashboard')
}

function onForgotPassword() {
  // TODO: 跳转忘记密码页
}

function onRegister() {
  // TODO: 跳转注册页
}
</script>

<style scoped>
/* ========== 页面框架 1440×800 ========== */
.login-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
}

.login-layout {
  width: 1440px;
  max-width: 100%;
  height: 100vh;
  margin: 0 auto;
  display: flex;
  overflow: hidden;
}

/* ========== 左侧品牌区 ========== */
.brand-panel {
  flex: 0 0 792px;
  background: #1a3b6b;
  color: #fff;
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-logo {
  display: flex;
  align-items: center;
}

.brand-logo__icon {
  flex: none;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #2b5ea7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.brand-logo__name {
  margin-left: 12px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-slogan__title {
  height: 52px;
  line-height: 52px;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
}

.brand-slogan__subtitle {
  height: 28px;
  line-height: 28px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40px;
}

.feature-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  height: 32px;
}

.feature-item__icon {
  flex: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(43, 94, 167, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #7ba7e0;
  margin-right: 12px;
}

.feature-item__text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.brand-copyright {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.brand-copyright i {
  font-size: 14px;
}

/* ========== 右侧登录区 ========== */
.login-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 40px;
}

.login-card__title {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: #1a3b6b;
  margin-bottom: 8px;
}

.login-card__subtitle {
  font-size: 14px;
  color: #666;
  margin-bottom: 32px;
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 24px;
}

.login-tab {
  flex: 1;
  height: 37px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.login-tab--active {
  background: #fff;
  color: #1a3b6b;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
}

.form-field {
  margin-bottom: 20px;
}

.form-field--password {
  margin-bottom: 16px;
}

.form-field__label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.form-field__label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-field__label-row .form-field__label {
  margin-bottom: 0;
}

.form-field__link {
  font-size: 12px;
  color: #2b5ea7;
}

.form-field__link:hover {
  text-decoration: underline;
}

.form-input {
  display: flex;
  align-items: center;
  height: 44px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 0 12px;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus-within {
  border-color: #2b5ea7;
  box-shadow: 0 0 0 3px rgba(43, 94, 167, 0.12);
}

.form-input__icon {
  flex: none;
  font-size: 18px;
  color: #999;
  margin-right: 10px;
}

.form-input input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  font-family: inherit;
  color: #333;
}

.form-input input::placeholder {
  color: #999;
}

.form-input__eye {
  flex: none;
  display: flex;
  align-items: center;
  border: none;
  background: transparent;
  padding: 0;
  font-size: 18px;
  color: #999;
  cursor: pointer;
}

/* 记住我 */
.login-options {
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.remember-me__checkbox {
  appearance: none;
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin: 0 8px 0 0;
  position: relative;
  transition: background 0.2s, border-color 0.2s;
}

.remember-me__checkbox::after {
  content: '\eb7b'; /* ri-check-line */
  font-family: 'remixicon';
  font-size: 12px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}

.remember-me__checkbox:checked {
  background: #2e7d32;
  border-color: #2e7d32;
}

.remember-me__checkbox:checked::after {
  opacity: 1;
}

.remember-me__label {
  font-size: 13px;
  color: #666;
  user-select: none;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 47px;
  border: none;
  border-radius: 6px;
  background: #1a3b6b;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.login-button:hover {
  background: #234a82;
}

.login-button:active {
  transform: translateY(1px);
}

.login-button:disabled {
  background: #8aa0bf;
  cursor: not-allowed;
}

.login-error {
  margin-top: 12px;
  font-size: 13px;
  color: #d32f2f;
}

/* 注册引导 */
.login-register {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
}

.login-register__text {
  color: #666;
}

.login-register__link {
  color: #2b5ea7;
  font-weight: 600;
  margin-left: 4px;
}

.login-register__link:hover {
  text-decoration: underline;
}

/* ========== 响应式：窄屏横向滚动保真 ========== */
@media (max-width: 1440px) {
  .login-layout {
    width: 1440px;
  }
}
</style>
