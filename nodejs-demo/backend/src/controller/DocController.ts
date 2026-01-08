import { Context } from 'koa';
import * as docService from '../service/docService';
import { success, error, notFound, forbidden } from '../util/response';
import { logger } from '../util/logger';

/**
 * 获取文档元数据
 */
export async function getMeta(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权访问该文档');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文档不存在');
    return;
  }

  success(ctx, doc);
}

/**
 * 更新文档元数据
 */
export async function updateMeta(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const { name, path } = ctx.request.body as any;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权修改该文档');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文档不存在');
    return;
  }

  const updatedDoc = await docService.updateDocMeta(docId, {
    name,
    path,
    modifiedAt: new Date(),
  });

  logger.info(`文档元数据更新成功: docId: ${docId}`);
  success(ctx, updatedDoc, '更新成功');
}

/**
 * 获取文档内容
 */
export async function getContent(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权访问该文档');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文档不存在');
    return;
  }

  const fileContent = await docService.getFileContent(docId);
  if (!fileContent) {
    notFound(ctx, '文档内容不存在');
    return;
  }

  ctx.status = 200;
  ctx.type = doc.mimeType || 'application/octet-stream';
  ctx.set('Content-Disposition', `attachment; filename="${encodeURIComponent(doc.name)}"`);
  ctx.body = fileContent;
}

/**
 * 上传文档内容
 */
export async function uploadContent(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权修改该文档');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文档不存在');
    return;
  }

  // 获取上传的文件
  const file = (ctx.request as any).file;
  if (!file) {
    error(ctx, '没有上传文件');
    return;
  }

  const { buffer, size } = file;
  await docService.uploadFile(docId, buffer, size);

  logger.info(`文档内容更新成功: docId: ${docId}`);
  success(ctx, null, '上传成功');
}

/**
 * 获取文档控制配置
 */
export async function getControl(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  let control = await docService.getDocControl(userId, docId);
  
  // 如果没有配置，返回默认配置
  if (!control) {
    const defaultControl = {
      id: '',
      userId,
      docId,
      canEdit: true,
      canDownload: true,
      canPrint: true,
      canCopy: true,
      canComment: true,
      canShare: true,
      watermarkEnabled: false,
      watermarkText: undefined,
      watermarkType: 'text',
      extensions: undefined,
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    success(ctx, defaultControl);
    return;
  }

  success(ctx, control);
}

/**
 * 更新文档控制配置
 */
export async function updateControl(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;
  const controlData = ctx.request.body as any;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    forbidden(ctx, '无权修改该文档配置');
    return;
  }

  const control = await docService.upsertDocControl(userId, docId, controlData);

  logger.info(`文档控制配置更新成功: docId: ${docId}`);
  success(ctx, control, '更新成功');
}

/**
 * 文档通知回调（用于 Filez 集成）
 */
export async function notify(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const notifyData = ctx.request.body as any;

  logger.info(`收到文档通知: docId: ${docId}`, notifyData);

  // 这里可以根据通知类型进行相应的处理
  // 例如：文档编辑完成、文档关闭等

  success(ctx, null, '通知已接收');
}

/**
 * 文档批注通知回调（用于 Filez 集成）
 */
export async function mention(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const mentionData = ctx.request.body as any;

  logger.info(`收到文档批注通知: docId: ${docId}`, mentionData);

  // 这里可以根据批注信息进行相应的处理
  // 例如：发送邮件通知、站内消息等

  success(ctx, null, '批注通知已接收');
}

