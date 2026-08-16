# upay-admin · PayHub 多通道支付聚合平台（管理端前端）

基于 **Vue 3 + Vite + Vue Router** 的支付聚合平台管理后台前端。

## 页面

- `/login` — PayHub 登录页（依据 Calicat 设计稿还原，1440×800）

## 技术栈

- [Vue 3](https://vuejs.org/)（`<script setup>` 组合式 API）
- [Vite 5](https://vitejs.dev/)
- [Vue Router 4](https://router.vuejs.org/)
- 图标：[remixicon](https://remixicon.com/)（CDN 引入）

## 快速开始

```bash
npm install
npm run dev        # 开发环境，默认 http://localhost:5173
npm run build      # 生产构建，产物输出到 dist/
npm run preview    # 预览构建产物
```

## 目录结构

```
upay-admin/
├── index.html
├── vite.config.js
├── public/
│   └── favicon.svg
└── src/
    ├── main.js
    ├── App.vue
    ├── router/
    │   └── index.js
    ├── styles/
    │   └── index.css
    └── views/
        └── login/
            └── LoginPage.vue
```

## 设计稿来源

- Calicat 设计文件：`2088804107962028032`
- 页面节点：`96cc58e1-d755-4c28-97a8-b1a28cf7b22d`（PayHub登录页，1440×800）

> 当前登录为前端演示（本地校验），登录接口待接入 hioas-upay 后端后替换 `LoginPage.vue` 中 `handleLogin` 的 TODO 逻辑。
