import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/files',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/files',
        name: 'Files',
        component: () => import('@/views/Files.vue'),
        meta: { requiresAuth: true, title: '文件列表' },
      },
      {
        path: '/user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { requiresAuth: true, title: '用户信息' },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

  if (requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next({ name: 'Login', query: { redirect: to.fullPath } });
    } else {
      // 已登录，初始化用户信息
      if (!userStore.userInfo) {
        try {
          await userStore.initUserInfo();
          next();
        } catch (error) {
          // 初始化失败，跳转到登录页
          next({ name: 'Login', query: { redirect: to.fullPath } });
        }
      } else {
        next();
      }
    }
  } else {
    // 不需要认证的页面
    if (to.name === 'Login' && userStore.isLoggedIn) {
      // 已登录用户访问登录页，跳转到首页
      next({ name: 'Files' });
    } else {
      next();
    }
  }
});

export default router;

