import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginPage.vue'),
      meta: { title: '登录 - PayHub' },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/dashboard/DashboardPage.vue'),
      meta: { title: '仪表板 - PayHub' },
    },
  ],
})

router.afterEach((to) => {
  const title = to.meta?.title
  if (title) document.title = title
})

export default router
