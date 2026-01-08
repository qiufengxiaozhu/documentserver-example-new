import * as fs from 'fs';
import * as path from 'path';
import { logger } from '../util/logger';

// 模板目录 - 使用进程工作目录，兼容开发和生产环境
const TEMPLATE_DIR = path.join(process.cwd(), 'templates');

// 文档类型到模板文件的映射
const TEMPLATE_FILES: Record<string, string> = {
  docx: 'new_doc.docx',
  xlsx: 'new_excel.xlsx',
  pptx: 'new_ppt.pptx',
};

// MIME 类型映射
const MIME_TYPES: Record<string, string> = {
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
};

/**
 * 获取模板文件内容和 MIME 类型
 */
export function getTemplate(docType: string): { buffer: Buffer | null; mimeType: string } {
  const templateFile = TEMPLATE_FILES[docType];
  
  if (!templateFile) {
    logger.warn(`不支持的文档类型: ${docType}`);
    return { buffer: null, mimeType: 'application/octet-stream' };
  }

  const templatePath = path.join(TEMPLATE_DIR, templateFile);
  
  if (!fs.existsSync(templatePath)) {
    logger.error(`模板文件不存在: ${templatePath}`);
    return { buffer: null, mimeType: 'application/octet-stream' };
  }

  try {
    const buffer = fs.readFileSync(templatePath);
    const mimeType = MIME_TYPES[docType] || 'application/octet-stream';
    logger.info(`使用模板文件: ${templateFile} (${buffer.length} bytes)`);
    return { buffer, mimeType };
  } catch (err) {
    logger.error(`读取模板文件失败: ${templatePath}`, err);
    return { buffer: null, mimeType: 'application/octet-stream' };
  }
}

/**
 * 获取 MIME 类型
 */
export function getMimeType(docType: string): string {
  return MIME_TYPES[docType] || 'application/octet-stream';
}

/**
 * 检查是否支持该文档类型
 */
export function isSupportedType(docType: string): boolean {
  return docType in TEMPLATE_FILES;
}

/**
 * 获取支持的文档类型列表
 */
export function getSupportedTypes(): string[] {
  return Object.keys(TEMPLATE_FILES);
}
