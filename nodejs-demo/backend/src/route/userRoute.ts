import Router from '@koa/router';
import { getUser, updateUser, changePassword, getAllUsers } from '../controller/UserController';
import { authMiddleware } from '../middleware/auth';

const router = new Router({ prefix: '/api/user' });

// 所有用户操作都需要认证
router.use(authMiddleware);

// 获取用户信息
router.get('/:userId', getUser);

// 更新用户信息
router.put('/:userId', updateUser);

// 修改密码
router.post('/:userId/password', changePassword);

// 获取所有用户（管理员）
router.get('/', getAllUsers);

export default router;

