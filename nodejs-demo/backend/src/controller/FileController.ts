import { Context } from 'koa';
import * as docService from '../service/docService';
import * as templateService from '../service/templateService';
import { success, error, notFound, forbidden } from '../util/response';
import { logger } from '../util/logger';
import * as path from 'path';

/**
 * 解码文件名（处理中文乱码）
 */
function decodeFileName(name: string): string {
  try {
    // multer 使用 Latin1 编码，需要转换为 UTF-8
    return Buffer.from(name, 'latin1').toString('utf8');
  } catch {
    return name;
  }
}

/**
 * 上传文件
 */
export async function upload(ctx: Context): Promise<void> {
  const file = (ctx.request as any).file;
  const userId = ctx.user?.userId;

  if (!file) {
    error(ctx, '没有上传文件');
    return;
  }

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  const { originalname: rawName, buffer, size, mimetype } = file;
  const originalname = decodeFileName(rawName);
  const extension = path.extname(originalname).substring(1);

  // 检查文件是否已存在
  const existingDoc = await docService.findByName(originalname);
  if (existingDoc) {
    error(ctx, '该文件已存在，请勿重复上传');
    return;
  }

  // 创建文档元数据
  const docMeta = await docService.createDocMeta({
    name: originalname,
    extension,
    mimeType: mimetype,
    createdById: userId,
    ownerId: userId,
  });

  // 保存文件
  await docService.uploadFile(docMeta.id, buffer, size);

  logger.info(`文件上传成功: ${originalname}, docId: ${docMeta.id}`);
  success(ctx, docMeta, '上传成功');
}

/**
 * 批量上传文件
 */
export async function batchUpload(ctx: Context): Promise<void> {
  const files = (ctx.request as any).files;
  const userId = ctx.user?.userId;

  if (!files || files.length === 0) {
    error(ctx, '没有上传文件');
    return;
  }

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  const results: string[] = [];

  for (const file of files) {
    try {
      const { originalname: rawName, buffer, size, mimetype } = file;
      const originalname = decodeFileName(rawName);
      const extension = path.extname(originalname).substring(1);

      // 检查文件是否已存在
      const existingDoc = await docService.findByName(originalname);
      if (existingDoc) {
        results.push(`上传 ${originalname} 失败: 文件已存在`);
        continue;
      }

      // 创建文档元数据
      const docMeta = await docService.createDocMeta({
        name: originalname,
        extension,
        mimeType: mimetype,
        createdById: userId,
        ownerId: userId,
      });

      // 保存文件
      await docService.uploadFile(docMeta.id, buffer, size);

      results.push(`上传 ${originalname} 成功`);
      logger.info(`文件上传成功: ${originalname}, docId: ${docMeta.id}`);
    } catch (err: any) {
      results.push(`上传 ${decodeFileName(file.originalname)} 失败: ${err.message}`);
      logger.error(`文件上传失败: ${file.originalname}`, err);
    }
  }

  success(ctx, results, '批量上传完成');
}

/**
 * 下载文件
 */
export async function download(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const downloadFlag = ctx.query.download !== 'false';
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权访问该文件');
    return;
  }

  // 获取文档信息
  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文件不存在');
    return;
  }

  // 获取文件内容
  const fileContent = await docService.getFileContent(docId);
  if (!fileContent) {
    notFound(ctx, '文件内容不存在');
    return;
  }

  // 设置响应头
  ctx.status = 200;
  ctx.type = doc.mimeType || 'application/octet-stream';
  
  if (downloadFlag) {
    ctx.set('Content-Disposition', `attachment; filename="${encodeURIComponent(doc.name)}"`);
  }

  ctx.body = fileContent;
  logger.info(`文件下载: ${doc.name}, docId: ${docId}`);
}

/**
 * 删除文件
 */
export async function deleteFile(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权删除该文件');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文件不存在');
    return;
  }

  await docService.deleteDoc(docId);
  logger.info(`文件删除成功: ${doc.name}, docId: ${docId}`);
  success(ctx, doc, '删除成功');
}

/**
 * 批量删除文件
 */
export async function batchDelete(ctx: Context): Promise<void> {
  const { fileIds } = ctx.request.body as any;
  const userId = ctx.user?.userId;

  if (!fileIds || !Array.isArray(fileIds)) {
    error(ctx, '参数错误');
    return;
  }

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  const results: string[] = [];

  for (const docId of fileIds) {
    try {
      // 检查权限
      const hasAccess = await docService.checkAccess(userId, docId);
      if (!hasAccess) {
        results.push(`删除 ${docId} 失败: 无权限`);
        continue;
      }

      const doc = await docService.findById(docId);
      if (!doc) {
        results.push(`删除 ${docId} 失败: 文件不存在`);
        continue;
      }

      await docService.deleteDoc(docId);
      results.push(`删除 ${doc.name}(id:${docId}) 成功`);
      logger.info(`文件删除成功: ${doc.name}, docId: ${docId}`);
    } catch (err: any) {
      results.push(`删除 ${docId} 失败: ${err.message}`);
      logger.error(`文件删除失败: ${docId}`, err);
    }
  }

  success(ctx, results, '批量删除完成');
}

/**
 * 新建文件
 */
export async function newFile(ctx: Context): Promise<void> {
  const { docType, filename } = ctx.request.body as any;
  const userId = ctx.user?.userId;

  if (!docType) {
    error(ctx, '文档类型不能为空');
    return;
  }

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查是否支持该文档类型
  if (!templateService.isSupportedType(docType)) {
    error(ctx, `不支持的文档类型: ${docType}，支持的类型: ${templateService.getSupportedTypes().join(', ')}`);
    return;
  }

  const timestamp = new Date().toISOString().replace(/[-:]/g, '').split('.')[0];
  const name = `${timestamp}-${filename || 'new'}.${docType}`;

  // 检查文件是否已存在
  const existingDoc = await docService.findByName(name);
  if (existingDoc) {
    error(ctx, '创建失败，文件名冲突');
    return;
  }

  // 获取模板内容
  const { buffer: templateBuffer, mimeType } = templateService.getTemplate(docType);
  if (!templateBuffer) {
    error(ctx, '获取模板文件失败');
    return;
  }

  // 创建文档元数据
  const docMeta = await docService.createDocMeta({
    name,
    extension: docType,
    mimeType,
    createdById: userId,
    ownerId: userId,
  });

  // 使用模板内容创建文件
  await docService.uploadFile(docMeta.id, templateBuffer, templateBuffer.length);

  logger.info(`新建文件成功: ${name}, docId: ${docMeta.id}, size: ${templateBuffer.length}`);
  success(ctx, docMeta, '创建成功');
}

/**
 * 获取文件列表
 */
export async function list(ctx: Context): Promise<void> {
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  const docs = await docService.listDocs(userId);
  success(ctx, docs);
}

