import jwt, { SignOptions } from 'jsonwebtoken';
import { appConfig as config } from '../config/AppConfig';

export interface JwtPayload {
  userId: string;
  username: string;
  email: string;
}

/**
 * 生成 JWT Token
 */
export function generateToken(payload: JwtPayload): string {
  const options: SignOptions = {
    expiresIn: config.jwt.expiresIn as any,
  };
  return jwt.sign(payload, config.jwt.secret, options);
}

/**
 * 验证 JWT Token
 */
export function verifyToken(token: string): JwtPayload {
  try {
    return jwt.verify(token, config.jwt.secret) as JwtPayload;
  } catch (error) {
    throw new Error('Invalid token');
  }
}

/**
 * 解码 JWT Token（不验证）
 */
export function decodeToken(token: string): JwtPayload | null {
  try {
    return jwt.decode(token) as JwtPayload;
  } catch (error) {
    return null;
  }
}

