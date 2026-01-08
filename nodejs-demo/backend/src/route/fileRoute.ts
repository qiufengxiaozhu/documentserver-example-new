import Router from '@koa/router';
import multer from '@koa/multer';
import {
  upload,
  batchUpload,
  download,
  deleteFile,
  batchDelete,
  newFile,
  list,
} from '../controller/FileController';
import { authMiddleware } from '../middleware/auth';
import { appConfig as config } from '../config/AppConfig';

const router = new Router({ prefix: '/api/file' });

// 配置 multer
const uploadMiddleware = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: config.upload.maxFileSize },
});

// 所有文件操作都需要认证
router.use(authMiddleware);

// 上传文件
router.post('/upload', uploadMiddleware.single('file'), upload);

// 批量上传文件
router.post('/batch-upload', uploadMiddleware.array('files', 10), batchUpload);

// 下载文件
router.get('/download/:docId', download);

// 删除文件
router.delete('/delete/:docId', deleteFile);

// 批量删除文件
router.post('/batch-delete', batchDelete);

// 新建文件
router.post('/new', newFile);

// 获取文件列表
router.get('/list', list);

export default router;

