import request from './request';
import { UserInfo } from './auth';

/**
 * 获取用户信息
 */
export function getUserInfo(userId: string) {
  return request.get<any, UserInfo>(`/api/user/${userId}`);
}

/**
 * 更新用户信息
 */
export function updateUserInfo(userId: string, data: {
  nickname?: string;
  avatar?: string;
  email?: string;
}) {
  return request.put<any, UserInfo>(`/api/user/${userId}`, data);
}

/**
 * 修改密码
 */
export function changePassword(userId: string, data: {
  oldPassword: string;
  newPassword: string;
}) {
  return request.post(`/api/user/${userId}/password`, data);
}

/**
 * 获取所有用户（管理员）
 */
export function getAllUsers() {
  return request.get<any, UserInfo[]>('/api/user');
}

