import { Context } from 'koa';
import * as docService from '../service/docService';
import * as userService from '../service/userService';
import { error, forbidden, notFound, success } from '../util/response';
import { logger } from '../util/logger';
import { hmacSign } from '../util/hmac';
import { appConfig } from '../config/AppConfig';

/**
 * 从请求中获取 token
 */
function getTokenFromRequest(ctx: Context): string | undefined {
  // 从 header 中获取
  const authHeader = ctx.headers.authorization;
  if (authHeader && authHeader.startsWith('Bearer ')) {
    return authHeader.substring(7);
  }

  // 从 cookie 中获取
  const cookieToken = ctx.cookies.get(appConfig.demo.tokenName);
  if (cookieToken) {
    return cookieToken;
  }

  // 从 query 中获取
  const queryToken = ctx.query[appConfig.demo.tokenName] as string;
  if (queryToken) {
    return queryToken;
  }

  return undefined;
}

// 使用 appConfig 中已处理好的配置（布尔值已正确转换）
const zofficeConfig = appConfig.zoffice;
const demoConfig = appConfig.demo;

/**
 * 用户 Profile 结构
 */
interface Profile {
  id: string;
  display_name: string;
  email: string;
  photo_url?: string;
  name: string;
  job_title?: string;
  org_name?: string;
  org_id?: string;
}

/**
 * 将用户转换为 Profile
 */
function convertUserToProfile(user: any): Profile {
  return {
    id: user.id,
    display_name: user.nickname || user.username,
    email: user.email || '',
    photo_url: user.avatar || '',
    name: user.username,
    job_title: user.jobTitle || '',
    org_name: user.orgName || '',
    org_id: user.orgId || '',
  };
}

/**
 * 构建 ZOffice 基础 URL
 */
function buildZOfficeBaseUrl(): string {
  let url = `${zofficeConfig.schema}://${zofficeConfig.host}`;
  if (zofficeConfig.port && zofficeConfig.port !== 80 && zofficeConfig.port !== 443) {
    url += `:${zofficeConfig.port}`;
  }
  return url;
}

/**
 * 获取前端集成 URL
 * GET /v2/context/driver-cb
 */
export async function getDriverCbUrl(ctx: Context): Promise<void> {
  const { docId, action = 'view', isInFrame = 'false' } = ctx.query as {
    docId: string;
    action?: string;
    isInFrame?: string;
  };

  if (!docId) {
    error(ctx, 'docId 参数不能为空');
    return;
  }

  const userId = ctx.user?.userId;
  const token = getTokenFromRequest(ctx);

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查是否启用前端集成
  if (!zofficeConfig.app.feIntegration.enable) {
    const url = standardMethodOpenDoc(docId, action, token);
    logger.info('使用标准集成方式', url);
    success(ctx, url);
    return;
  }

  // 前端集成方式
  const baseUrl = buildZOfficeBaseUrl();
  const params = new URLSearchParams();

  // 注意：参数顺序要保持一致
  params.append('repoId', demoConfig.repoId);
  params.append('action', action);
  params.append('docId', docId);

  // 1. 传输用户信息
  const user = await userService.findById(userId);
  if (user) {
    const profile = convertUserToProfile(user);
    const userinfo = Buffer.from(JSON.stringify(profile)).toString('base64');
    params.append('userinfo', userinfo);
  }

  // 2. 传输文档元信息（必填）
  const docMeta = await docService.findById(docId);
  if (docMeta) {
    const docControl = await docService.getDocControl(userId, docId);
    const metaObj = {
      id: docMeta.id,
      name: docMeta.name,
      size: docMeta.size,
      version: String(docMeta.version || '1'),
      filepath: docMeta.path,
      created_at: formatDateToUtc(docMeta.createdAt),
      modified_at: formatDateToUtc(docMeta.modifiedAt || docMeta.updatedAt),
      created_by: userToZOfficeProfile(docMeta.createdBy),
      modified_by: userToZOfficeProfile(docMeta.createdBy),
      owner: userToZOfficeProfile(docMeta.owner),
      permissions: {
        write: docControl?.canEdit ?? true,
        read: true,
        download: docControl?.canDownload ?? true,
        print: docControl?.canPrint ?? true,
        copy: docControl?.canCopy ?? true,
      },
    };
    const metainfo = Buffer.from(JSON.stringify(metaObj)).toString('base64');
    params.append('meta', metainfo);
  }

  // 3. 文档下载/上传地址
  const serverPort = appConfig.server.port;
  const downloadOrUploadUrl = `http://${demoConfig.host}:${serverPort}${demoConfig.context}/${docId}/content`;
  params.append('downloadUrl', downloadOrUploadUrl);
  params.append('uploadUrl', downloadOrUploadUrl);

  // 4. 请求体的认证信息
  const header = `${demoConfig.tokenName}=${token};param-1=aaa;x-param-2=bbb`;
  params.append('params', header);

  // 5. 当前时间戳
  params.append('ts', Date.now().toString());

  // 6. 计算 HMAC
  const fullUrl = `${baseUrl}${zofficeConfig.context}?${params.toString()}`;
  const hmac = hmacSign(fullUrl, zofficeConfig.app.secret);
  params.append('HMAC', hmac);

  const finalUrl = `${baseUrl}${zofficeConfig.context}?${params.toString()}`;
  logger.info(`前端集成完整URL: ${finalUrl}`);

  if (isInFrame === 'true') {
    success(ctx, `/home/iframe?url=${encodeURIComponent(finalUrl)}`);
  } else {
    success(ctx, finalUrl);
  }
}

/**
 * 标准集成方式打开文件
 */
function standardMethodOpenDoc(docId: string, action: string, token?: string): string {
  const baseUrl = buildZOfficeBaseUrl();
  const path = `/docs/app/${demoConfig.repoId}/${docId}/${action}/content`;
  const params = new URLSearchParams();
  if (token) {
    params.append(demoConfig.tokenName, token);
  }
  return `${baseUrl}${path}?${params.toString()}`;
}

/**
 * 下载文件内容
 * GET /v2/context/:docId/content
 */
export async function getDocContent(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const { download = 'false' } = ctx.query as { download?: string };
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    logger.error(`用户 ${userId} 无权访问文件 ${docId}`);
    forbidden(ctx, '无权访问该文件');
    return;
  }

  // 获取文件内容
  const fileContent = await docService.getFileContent(docId);
  if (!fileContent) {
    logger.error('文件不存在，下载失败');
    notFound(ctx, '文件不存在');
    return;
  }

  const doc = await docService.findById(docId);
  if (!doc) {
    notFound(ctx, '文档不存在');
    return;
  }

  ctx.status = 200;
  ctx.type = doc.mimeType || 'application/octet-stream';

  if (download === 'true') {
    const encodedName = encodeURIComponent(doc.name);
    ctx.set('Content-Disposition', `attachment; filename="${encodedName}"; filename*=UTF-8''${encodedName}`);
  }

  ctx.body = fileContent;
  logger.info(`文件下载成功: docId=${docId}, name=${doc.name}`);
}

/**
 * 上传文件内容（zoffice 编辑后回传）
 * POST /v2/context/:docId/content
 */
export async function publishDoc(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    ctx.status = 403;
    ctx.body = null;
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    logger.error(`用户 ${userId} 无权访问文件 ${docId}`);
    ctx.status = 403;
    ctx.body = null;
    return;
  }

  const docMeta = await docService.findById(docId);
  if (!docMeta) {
    ctx.status = 404;
    ctx.body = null;
    return;
  }

  // 获取上传的文件
  const file = (ctx.request as any).file;
  if (!file) {
    // 没有文件时，返回当前文档元数据（ZOffice 格式）
    ctx.status = 200;
    ctx.body = docMetaToZOfficeFormat(docMeta);
    return;
  }

  try {
    const { buffer, size } = file;
    const newDocMeta = await docService.uploadFile(docId, buffer, size);

    if (newDocMeta && newDocMeta.modifiedAt && docMeta.modifiedAt) {
      const oldTime = new Date(docMeta.modifiedAt).getTime();
      const newTime = new Date(newDocMeta.modifiedAt).getTime();
      if (oldTime < newTime) {
        logger.info(`文件上传成功，保存时间：${newDocMeta.modifiedAt}`);
        // 上传成功，返回新的文档元数据（ZOffice 格式）
        ctx.status = 200;
        ctx.body = docMetaToZOfficeFormat(newDocMeta);
        return;
      }
    }
    logger.info('文件保存失败');
  } catch (e) {
    logger.error('上传文件失败', e);
  }

  // 上传失败，返回原文档元数据（ZOffice 格式）
  ctx.status = 200;
  ctx.body = docMetaToZOfficeFormat(docMeta);
}

/**
 * 将日期格式化为 ISO 8601 UTC 格式
 * 格式: 2020-03-25T02:57:38.000Z
 */
function formatDateToUtc(date: Date | string | undefined): string {
  if (!date) {
    return new Date().toISOString();
  }
  if (typeof date === 'string') {
    return new Date(date).toISOString();
  }
  return date.toISOString();
}

/**
 * 将用户转换为 ZOffice Profile 格式
 */
function userToZOfficeProfile(user: any): any {
  if (!user) return undefined;
  return {
    id: user.id,
    display_name: user.nickname || user.username,
    email: user.email,
    photo_url: user.avatar || '',
    name: user.username,
    job_title: user.jobTitle || '',
    org_name: user.orgName || '',
    org_id: user.orgId || '',
  };
}

/**
 * 将 DocMeta 转换为 ZOffice 需要的格式（下划线命名）
 * 用于 publishDoc 接口返回
 */
function docMetaToZOfficeFormat(docMeta: any): any {
  return {
    id: docMeta.id,
    name: docMeta.name,
    size: docMeta.size,
    version: String(docMeta.version || '1'),
    filepath: docMeta.path || '',
    // 时间字段（必须为 ISO 8601 UTC 格式）
    created_at: formatDateToUtc(docMeta.createdAt),
    modified_at: formatDateToUtc(docMeta.modifiedAt || docMeta.updatedAt),
    // 用户信息
    created_by: userToZOfficeProfile(docMeta.createdBy),
    modified_by: userToZOfficeProfile(docMeta.createdBy),
    owner: userToZOfficeProfile(docMeta.owner),
  };
}

/**
 * 获取文档元数据
 * GET /v2/context/:docId/meta
 */
export async function getDocMeta(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const userId = ctx.user?.userId;

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查权限
  const hasAccess = await docService.checkAccess(userId, docId);
  if (!hasAccess) {
    logger.error(`用户 ${userId} 无权查询文件 ${docId}`);
    forbidden(ctx, '无权查询该文件');
    return;
  }

  const docMeta = await docService.findById(docId);
  if (!docMeta) {
    notFound(ctx, '文档不存在');
    return;
  }

  // 获取文档控制配置（权限）
  const docControl = await docService.getDocControl(userId, docId);

  // 构建符合 zoffice 格式的响应（字段使用下划线命名）
  const response: any = {
    id: docMeta.id,
    name: docMeta.name,
    size: docMeta.size,
    version: String(docMeta.version || '1'),
    filepath: docMeta.path,
    // 时间字段（必须为 ISO 8601 UTC 格式）
    created_at: formatDateToUtc(docMeta.createdAt),
    modified_at: formatDateToUtc(docMeta.modifiedAt || docMeta.updatedAt),
    // 用户信息（必填）
    created_by: userToZOfficeProfile(docMeta.createdBy),
    modified_by: userToZOfficeProfile(docMeta.createdBy), // 如果没有 modifiedBy，使用 createdBy
    owner: userToZOfficeProfile(docMeta.owner),
    // 权限配置（必填）
    permissions: {
      write: docControl?.canEdit ?? true,
      read: true,
      download: docControl?.canDownload ?? true,
      print: docControl?.canPrint ?? true,
      copy: docControl?.canCopy ?? true,
    },
  };

  // 水印配置（可选）
  if (docControl?.watermarkEnabled) {
    response.waterMark = {
      enabled: true,
      text: docControl.watermarkText || '',
      type: docControl.watermarkType || 'text',
    };
  }

  ctx.body = response;
}

/**
 * 获取用户信息
 * GET /v2/context/profiles
 */
export async function getUserProfiles(ctx: Context): Promise<void> {
  const { users, keyword, page_num, page_limit } = ctx.query as {
    users?: string | string[];
    keyword?: string;
    page_num?: string;
    page_limit?: string;
  };

  const currentUserId = ctx.user?.userId;

  // 根据关键字分页查询
  if (keyword || page_num || page_limit) {
    const allUsers = await userService.getAllUsers();
    const profiles = allUsers.map(convertUserToProfile);

    ctx.body = {
      total: profiles.length,
      items: profiles,
    };
    return;
  }

  // 查询指定用户列表
  if (users) {
    const userIds = Array.isArray(users) ? users : [users];
    const profileList: Profile[] = [];

    for (const uid of userIds) {
      const user = await userService.findById(uid);
      if (user) {
        profileList.push(convertUserToProfile(user));
      }
    }

    ctx.body = {
      total: profileList.length,
      list: profileList,
    };
    return;
  }

  // 返回当前用户
  if (currentUserId) {
    const currentUser = await userService.findById(currentUserId);
    if (currentUser) {
      ctx.body = convertUserToProfile(currentUser);
      return;
    }
  }

  ctx.body = null;
}

/**
 * 文档打开状态通知
 * POST /v2/context/:docId/notify
 */
export async function docsNotify(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const body = ctx.request.body as any;

  // body 示例: {"docId":"demo-doc","repoId":"thirdparty-rest","type":"edit.session.close"}
  logger.info(`来自 zOffice notify: docId=${docId}`, body);

  ctx.body = body;
}

/**
 * 文档批注通知
 * POST /v2/context/:docId/mention
 */
export async function mention(ctx: Context): Promise<void> {
  const { docId } = ctx.params;
  const body = ctx.request.body as any;

  logger.info(`来自 zOffice 的提醒: docId=${docId}`, body);

  ctx.body = 'success';
}

/**
 * 文档另存预检
 * OPTIONS /v2/context/files/content
 */
export async function preflightCheck(ctx: Context): Promise<void> {
  const body = ctx.request.body as any;
  const { name, parentPathName } = body;

  logger.info(`另存预检: name=${name}, path=${parentPathName}`);

  // 创建新文件
  try {
    const docMeta = await docService.makeNewFile(name, parentPathName || '/');
    if (!docMeta) {
      ctx.status = 409;
      ctx.body = { error: 'preflight check fail' };
      return;
    }
    ctx.body = docMeta;
  } catch (err) {
    ctx.status = 409;
    ctx.body = { error: 'preflight check fail' };
  }
}

/**
 * 文档对比 URL
 * GET /v2/context/compareDoc
 */
export async function compareDoc(ctx: Context): Promise<void> {
  const { docAid, docBid } = ctx.query as { docAid: string; docBid: string };

  if (!docAid || !docBid) {
    error(ctx, 'docAid 和 docBid 参数不能为空');
    return;
  }

  const userId = ctx.user?.userId;
  const token = getTokenFromRequest(ctx);

  if (!userId) {
    forbidden(ctx, '未登录');
    return;
  }

  // 检查是否启用前端集成
  if (!zofficeConfig.app.feIntegration.enable) {
    logger.info('使用标准集成方式比较文档');
    const url = standardMethodCompare(docAid, docBid, token);
    ctx.body = url;
    return;
  }

  // 前端集成方式
  const baseUrl = buildZOfficeBaseUrl();
  const params = new URLSearchParams();

  params.append('repoId', demoConfig.repoId);
  params.append('action', 'compare');
  params.append('docId', docAid);
  params.append('docIdB', docBid);

  // 1. 传输用户信息
  const user = await userService.findById(userId);
  if (user) {
    const profile = convertUserToProfile(user);
    const userinfo = Buffer.from(JSON.stringify(profile)).toString('base64');
    params.append('userinfo', userinfo);
  }

  // 2. 传输文档元信息
  const docMetaA = await docService.findById(docAid);
  if (docMetaA) {
    const metainfo = Buffer.from(JSON.stringify(docMetaA)).toString('base64');
    params.append('meta', metainfo);
  }
  const docMetaB = await docService.findById(docBid);
  if (docMetaB) {
    const metainfoB = Buffer.from(JSON.stringify(docMetaB)).toString('base64');
    params.append('metaB', metainfoB);
  }

  // 3. 下载地址
  const serverPort = appConfig.server.port;
  const downloadUrlA = `http://${demoConfig.host}:${serverPort}${demoConfig.context}/${docAid}/content`;
  const downloadUrlB = `http://${demoConfig.host}:${serverPort}${demoConfig.context}/${docBid}/content`;
  params.append('downloadUrl', downloadUrlA);
  params.append('downloadUrlB', downloadUrlB);

  // 4. 请求体的认证信息
  const header = `${demoConfig.tokenName}=${token};param-1=aaa;x-param-2=bbb`;
  params.append('params', header);

  // 5. 当前时间戳
  params.append('ts', Date.now().toString());

  // 6. 计算 HMAC
  const fullUrl = `${baseUrl}${zofficeConfig.context}?${params.toString()}`;
  const hmac = hmacSign(fullUrl, zofficeConfig.app.secret);
  params.append('HMAC', hmac);

  const finalUrl = `${baseUrl}${zofficeConfig.context}?${params.toString()}`;
  logger.info(`文档比对完整URL: ${finalUrl}`);

  ctx.body = finalUrl;
}

/**
 * 标准集成方式比较文档
 */
function standardMethodCompare(docAid: string, docBid: string, token?: string): string {
  const baseUrl = buildZOfficeBaseUrl();
  const path = `/docs/app/${demoConfig.repoId}/compare`;
  const params = new URLSearchParams();
  params.append('docA', docAid);
  params.append('docB', docBid);
  if (token) {
    params.append(demoConfig.tokenName, token);
  }
  return `${baseUrl}${path}?${params.toString()}`;
}

