import Router from '@koa/router';
import multer from '@koa/multer';
import {
  getDriverCbUrl,
  getDocContent,
  publishDoc,
  getDocMeta,
  getUserProfiles,
  docsNotify,
  mention,
  preflightCheck,
  compareDoc,
} from '../controller/ZOfficeController';
import { authMiddleware } from '../middleware/auth';

const router = new Router({ prefix: '/v2/context', });

// 配置 multer
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 500 * 1024 * 1024 }, // 500MB
});

// 获取前端集成 URL
router.get('/driver-cb', authMiddleware, getDriverCbUrl);

// 文档内容下载（zoffice 服务器回调会携带 token）
router.get('/:docId/content', authMiddleware, getDocContent);

// 文档内容上传（zoffice 编辑后回传）
router.post('/:docId/content', authMiddleware, upload.single('file'), publishDoc);

// 获取文档元数据
router.get('/:docId/meta', authMiddleware, getDocMeta);

// 获取用户信息
router.get('/profiles', authMiddleware, getUserProfiles);

// 文档打开状态通知（zoffice 服务器回调会携带 token）
router.post('/:docId/notify', authMiddleware, docsNotify);

// 文档批注通知（zoffice 服务器回调会携带 token）
router.post('/:docId/mention', authMiddleware, mention);

// 文档另存预检
router.options('/files/content', preflightCheck);
router.post('/files/content', authMiddleware, preflightCheck);

// 文档对比
router.get('/compareDoc', authMiddleware, compareDoc);

export default router;

