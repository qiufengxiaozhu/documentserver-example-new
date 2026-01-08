/**
 * 下载文件
 */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

/**
 * 从响应中下载文件
 */
export function downloadFromResponse(response: any, defaultFilename: string = 'download'): void {
  // 尝试从响应头中获取文件名
  let filename = defaultFilename;
  const contentDisposition = response.headers['content-disposition'];
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
    if (filenameMatch && filenameMatch[1]) {
      filename = filenameMatch[1].replace(/['"]/g, '');
      // 解码 URL 编码的文件名
      try {
        filename = decodeURIComponent(filename);
      } catch (e) {
        // 如果解码失败，使用原始文件名
      }
    }
  }
  
  downloadBlob(response.data, filename);
}

