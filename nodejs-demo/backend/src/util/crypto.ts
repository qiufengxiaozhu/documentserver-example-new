import * as crypto from 'crypto';

/**
 * SHA256 哈希
 */
export function sha256(text: string): string {
  return crypto.createHash('sha256').update(text).digest('hex');
}

/**
 * 密码加密
 */
export function hashPassword(password: string): string {
  return sha256(password);
}

/**
 * 验证密码
 */
export function verifyPassword(password: string, hash: string): boolean {
  return hashPassword(password) === hash;
}
