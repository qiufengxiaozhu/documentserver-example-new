import Router from '@koa/router';
import { login, logout, profile, profiles } from '../controller/AuthController';
import { authMiddleware } from '../middleware/auth';

const router = new Router({ prefix: '/api/auth' });

// 登录
router.post('/login', login);

// 登出
router.post('/logout', logout);

// 获取当前用户信息（需要认证）
router.get('/profile', authMiddleware, profile);

// 获取用户列表（用于 Filez 集成）
router.get('/profiles', authMiddleware, profiles);

export default router;

