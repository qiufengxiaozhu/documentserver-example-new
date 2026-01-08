import Router from '@koa/router';
import multer from '@koa/multer';
import {
  getMeta,
  updateMeta,
  getContent,
  uploadContent,
  getControl,
  updateControl,
  notify,
  mention,
} from '../controller/DocController';
import { authMiddleware } from '../middleware/auth';
import { appConfig as config } from '../config/AppConfig';

const router = new Router({ prefix: '/api/doc' });

// 配置 multer
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: config.upload.maxFileSize },
});

// 所有文档操作都需要认证
router.use(authMiddleware);

// 获取文档元数据
router.get('/:docId/meta', getMeta);

// 更新文档元数据
router.put('/:docId/meta', updateMeta);

// 获取文档内容
router.get('/:docId/content', getContent);

// 上传文档内容
router.post('/:docId/content', upload.single('file'), uploadContent);

// 获取文档控制配置
router.get('/:docId/control', getControl);

// 更新文档控制配置
router.put('/:docId/control', updateControl);

// 文档通知回调
router.post('/:docId/notify', notify);

// 文档批注通知回调
router.post('/:docId/mention', mention);

export default router;

