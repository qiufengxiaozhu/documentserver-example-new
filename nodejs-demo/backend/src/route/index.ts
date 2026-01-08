import Router from '@koa/router';
import authRoute from './authRoute';
import fileRoute from './fileRoute';
import docRoute from './docRoute';
import userRoute from './userRoute';
import zofficeRoute from './zofficeRoute';

const router = new Router();

// 健康检查
router.get('/health', (ctx) => {
  ctx.body = {
    status: 'ok',
    timestamp: new Date().toISOString(),
  };
});

// 注册各模块路由
router.use(authRoute.routes(), authRoute.allowedMethods());
router.use(fileRoute.routes(), fileRoute.allowedMethods());
router.use(docRoute.routes(), docRoute.allowedMethods());
router.use(userRoute.routes(), userRoute.allowedMethods());

// ZOffice 集成路由 (路径: /v2/context/*)
router.use(zofficeRoute.routes(), zofficeRoute.allowedMethods());

export default router;

