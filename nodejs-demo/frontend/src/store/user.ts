import { defineStore } from 'pinia';
import { ref } from 'vue';
import { login as loginApi, logout as logoutApi, getProfile, type UserInfo, type LoginParams } from '@/api/auth';
import { ElMessage } from 'element-plus';

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '');
  const userInfo = ref<UserInfo | null>(null);
  const isLoggedIn = ref<boolean>(!!token.value);

  /**
   * 登录
   */
  async function login(params: LoginParams) {
    try {
      const res = await loginApi(params);
      token.value = res.token;
      userInfo.value = res.user;
      isLoggedIn.value = true;
      
      // 保存 token 到 localStorage
      localStorage.setItem('token', res.token);
      
      ElMessage.success('登录成功');
      return true;
    } catch (error) {
      console.error('登录失败:', error);
      return false;
    }
  }

  /**
   * 登出
   */
  async function logout() {
    try {
      await logoutApi();
    } catch (error) {
      console.error('登出失败:', error);
    } finally {
      token.value = '';
      userInfo.value = null;
      isLoggedIn.value = false;
      localStorage.removeItem('token');
      ElMessage.success('已登出');
    }
  }

  /**
   * 获取用户信息
   */
  async function fetchUserInfo() {
    try {
      const info = await getProfile();
      userInfo.value = info;
      return info;
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果获取失败，清除登录状态
      token.value = '';
      userInfo.value = null;
      isLoggedIn.value = false;
      localStorage.removeItem('token');
      throw error;
    }
  }

  /**
   * 初始化用户信息
   */
  async function initUserInfo() {
    if (token.value && !userInfo.value) {
      try {
        await fetchUserInfo();
      } catch (error) {
        console.error('初始化用户信息失败:', error);
      }
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo,
    initUserInfo,
  };
});

