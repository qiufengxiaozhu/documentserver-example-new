import request from './request';
import { DocMeta } from './file';

export interface DocControl {
  id: string;
  userId: string;
  docId: string;
  canEdit: boolean;
  canDownload: boolean;
  canPrint: boolean;
  canCopy: boolean;
  canComment: boolean;
  canShare: boolean;
  watermarkEnabled: boolean;
  watermarkText?: string;
  watermarkType?: string;
  extensions?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 获取文档元数据
 */
export function getDocMeta(docId: string) {
  return request.get<any, DocMeta>(`/api/doc/${docId}/meta`);
}

/**
 * 更新文档元数据
 */
export function updateDocMeta(docId: string, data: { name?: string; path?: string }) {
  return request.put<any, DocMeta>(`/api/doc/${docId}/meta`, data);
}

/**
 * 获取文档内容
 */
export function getDocContent(docId: string) {
  return request.get(`/api/doc/${docId}/content`, {
    responseType: 'blob',
  });
}

/**
 * 上传文档内容
 */
export function uploadDocContent(docId: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post(`/api/doc/${docId}/content`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
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
export function updateDocControl(docId: string, data: Partial<DocControl>) {
  return request.put<any, DocControl>(`/api/doc/${docId}/control`, data);
}

