import crypto from 'crypto';

/**
 * 使用 HMAC-SHA256 生成签名
 * @param url 请求 URL，支持完整 URL 或相对路径（如 /api/hello?name=张三）
 * @param secret 加密用的密钥
 * @returns 64 位小写十六进制字符串
 */
export function hmacSign(url: string, secret: string): string {
  // 解析 URL，支持完整 URL 和相对路径
  const urlObj = new URL(url, 'http://placeholder');
  const path = urlObj.pathname;
  const query = urlObj.search.slice(1); // 去掉开头的 ?
  
  // 构造要签名的字符串
  const requestData = query ? `${path}?${query}` : path;
  
  // 使用 HMAC-SHA256 生成签名
  const hmac = crypto.createHmac('sha256', secret);
  hmac.update(requestData);
  
  // 返回 64 位十六进制字符串
  return hmac.digest('hex').padStart(64, '0');
}
