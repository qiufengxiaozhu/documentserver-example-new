import request from './request';

export interface DocMeta {
  id: string;
  name: string;
  path: string;
  size: number;
  extension: string;
  mimeType?: string;
  version: number;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
  modifiedAt?: string;
  createdBy?: {
    id: string;
    username: string;
    email: string;
    nickname?: string;
  };
  owner?: {
    id: string;
    username: string;
    email: string;
    nickname?: string;
  };
}

/**
 * 上传文件
 */
export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<any, DocMeta>('/api/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

/**
 * 批量上传文件
 */
export function batchUploadFiles(files: File[]) {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append('files', file);
  });
  return request.post<any, string[]>('/api/file/batch-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

/**
 * 下载文件
 */
export function downloadFile(docId: string, download: boolean = true) {
  return request.get(`/api/file/download/${docId}`, {
    params: { download },
    responseType: 'blob',
  });
}

/**
 * 删除文件
 */
export function deleteFile(docId: string) {
  return request.delete<any, DocMeta>(`/api/file/delete/${docId}`);
}

/**
 * 批量删除文件
 */
export function batchDeleteFiles(fileIds: string[]) {
  return request.post<any, string[]>('/api/file/batch-delete', { fileIds });
}

/**
 * 新建文件
 */
export function newFile(docType: string, filename?: string) {
  return request.post<any, DocMeta>('/api/file/new', { docType, filename });
}

/**
 * 获取文件列表
 */
export function getFileList() {
  return request.get<any, DocMeta[]>('/api/file/list');
}

/**
 * 更新文档元数据
 */
export interface UpdateDocMetaParams {
  name?: string;
  path?: string;
}

export function updateDocMeta(docId: string, data: UpdateDocMetaParams) {
  return request.put<any, DocMeta>(`/api/doc/${docId}/meta`, data);
}

/**
 * 文档控制配置
 */
export interface DocControl {
  id: string;
  userId: string;
  docId: string;
  // 权限控制
  canEdit: boolean;
  canDownload: boolean;
  canPrint: boolean;
  canCopy: boolean;
  canComment: boolean;
  canShare: boolean;
  // 水印配置
  watermarkEnabled: boolean;
  watermarkText?: string;
  watermarkType?: string;
  // 扩展配置
  extensions?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 获取文档控制配置
 */
export function getDocControl(docId: string) {
  return request.get<any, DocControl>(`/api/doc/${docId}/control`);
}

/**
 * 更新文档控制配置
 */
export interface UpdateDocControlParams {
  canEdit?: boolean;
  canDownload?: boolean;
  canPrint?: boolean;
  canCopy?: boolean;
  canComment?: boolean;
  canShare?: boolean;
  watermarkEnabled?: boolean;
  watermarkText?: string;
  watermarkType?: string;
  extensions?: string;
}

export function updateDocControl(docId: string, data: UpdateDocControlParams) {
  return request.put<any, DocControl>(`/api/doc/${docId}/control`, data);
}

/**
 * 获取 ZOffice 集成 URL（用于预览/编辑）
 * @param docId 文档ID
 * @param action 操作类型: view | edit
 * @param isInFrame 是否在 iframe 中打开
 */
export function getZOfficeUrl(docId: string, action: 'view' | 'edit' = 'view', isInFrame: boolean = false) {
  return request.get<any, string>('/v2/context/driver-cb', {
    params: { docId, action, isInFrame },
  });
}

