<template>
  <div class="user-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <h2>用户信息</h2>
        </div>
      </template>

      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        class="user-form"
      >
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>

        <el-form-item label="头像URL" prop="avatar">
          <el-input v-model="userForm.avatar" />
        </el-form-item>

        <el-form-item label="创建时间">
          <el-input :value="formatDate(userForm.createdAt)" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUpdate">
            更新信息
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="password-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <h2>修改密码</h2>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="passwordLoading"
            @click="handleChangePassword"
          >
            修改密码
          </el-button>
          <el-button @click="handlePasswordReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { updateUserInfo, changePassword } from '@/api/user';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

const userStore = useUserStore();
const userFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();
const loading = ref(false);
const passwordLoading = ref(false);

const userForm = reactive({
  id: '',
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  createdAt: '',
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
};

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
};

onMounted(() => {
  loadUserInfo();
});

const loadUserInfo = () => {
  if (userStore.userInfo) {
    Object.assign(userForm, userStore.userInfo);
  }
};

const handleUpdate = async () => {
  if (!userFormRef.value) return;

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await updateUserInfo(userForm.id, {
          email: userForm.email,
          nickname: userForm.nickname,
          avatar: userForm.avatar,
        });
        
        // 更新 store 中的用户信息
        await userStore.fetchUserInfo();
        
        ElMessage.success('更新成功');
      } catch (error) {
        ElMessage.error('更新失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

const handleReset = () => {
  loadUserInfo();
};

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true;
      try {
        await changePassword(userForm.id, {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        
        ElMessage.success('密码修改成功');
        handlePasswordReset();
      } catch (error) {
        ElMessage.error('密码修改失败');
      } finally {
        passwordLoading.value = false;
      }
    }
  });
};

const handlePasswordReset = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

const formatDate = (date: string): string => {
  if (!date) return '-';
  const d = new Date(date);
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};
</script>

<style scoped>
.user-container {
  max-width: 800px;
}

.card-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.user-form,
.password-form {
  max-width: 600px;
}
</style>

