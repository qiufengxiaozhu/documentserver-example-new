<template>
  <div class="layout-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <h1>Filez Demo</h1>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="user">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="layout-aside">
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/files">
              <el-icon><Folder /></el-icon>
              <span>文件列表</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>用户信息</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/user';
import { ElMessageBox } from 'element-plus';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const activeMenu = computed(() => route.path);

const handleCommand = (command: string) => {
  if (command === 'user') {
    router.push('/user');
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      await userStore.logout();
      router.push('/login');
    }).catch(() => {
      // 取消
    });
  }
};
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.el-container {
  height: 100%;
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left h1 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-dropdown span {
  margin: 0 5px;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e6e6e6;
}

.sidebar-menu {
  border-right: none;
}

.layout-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>

