import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import { config } from '@/config';

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: config.apiBaseURL,
  timeout: 30000,
  withCredentials: true, // 允许携带 cookie
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response;
    
    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response;
    }

    // 处理业务错误
    if (data.code !== undefined && data.code !== 0) {
      ElMessage.error(data.message || '请求失败');
      return Promise.reject(new Error(data.message || '请求失败'));
    }

    // 返回 data.data，这是实际的业务数据
    return data.data;
  },
  (error) => {
    console.error('响应错误:', error);

    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          ElMessage.error('未登录或登录已过期，请重新登录');
          // 清除 token
          localStorage.removeItem('token');
          // 跳转到登录页
          window.location.href = '/login';
          break;
        case 403:
          ElMessage.error(data.message || '没有权限访问');
          break;
        case 404:
          ElMessage.error(data.message || '请求的资源不存在');
          break;
        case 500:
          ElMessage.error(data.message || '服务器错误');
          break;
        default:
          ElMessage.error(data.message || '请求失败');
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接');
    } else {
      ElMessage.error(error.message || '请求失败');
    }

    return Promise.reject(error);
  }
);

export default request;

