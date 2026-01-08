/**
 * 应用配置
 */
export const config = {
  /** API 基础地址 - 生产环境使用相对路径，开发环境使用代理 */
  apiBaseURL: import.meta.env.VITE_API_BASE_URL || '',
};

