import request from './request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface UserInfo {
  id: string;
  username: string;
  email: string;
  nickname?: string;
  avatar?: string;
  createdAt?: string;
}

export interface LoginResponse {
  token: string;
  user: UserInfo;
}

/**
 * 用户登录
 */
export function login(data: LoginParams) {
  return request.post<any, LoginResponse>('/api/auth/login', data);
}

/**
 * 用户登出
 */
export function logout() {
  return request.post('/api/auth/logout');
}

/**
 * 获取当前用户信息
 */
export function getProfile() {
  return request.get<any, UserInfo>('/api/auth/profile');
}

/**
 * 获取用户列表
 */
export function getProfiles() {
  return request.get<any, UserInfo[]>('/api/auth/profiles');
}

