/**
 * Swagger API 文档配置
 */
export const swaggerDocument = {
  openapi: '3.0.0',
  info: {
    title: 'Filez Demo API',
    version: '1.0.0',
    description: '文档管理系统后端 API',
  },
  servers: [
    {
      url: '/api',
      description: 'API Server',
    },
  ],
  tags: [
    { name: 'auth', description: '认证相关' },
    { name: 'file', description: '文件管理' },
    { name: 'doc', description: '文档操作' },
    { name: 'user', description: '用户管理' },
  ],
  paths: {
    '/auth/login': {
      post: {
        tags: ['auth'],
        summary: '用户登录',
        requestBody: {
          required: true,
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  username: { type: 'string', example: 'admin' },
                  password: { type: 'string', example: 'zOffice' },
                },
                required: ['username', 'password'],
              },
            },
          },
        },
        responses: {
          200: { description: '登录成功' },
          401: { description: '用户名或密码错误' },
        },
      },
    },
    '/auth/logout': {
      post: {
        tags: ['auth'],
        summary: '用户登出',
        responses: {
          200: { description: '登出成功' },
        },
      },
    },
    '/auth/profile': {
      get: {
        tags: ['auth'],
        summary: '获取当前用户信息',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: '成功' },
          401: { description: '未授权' },
        },
      },
    },
    '/auth/profiles': {
      get: {
        tags: ['auth'],
        summary: '获取用户列表',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: '成功' },
        },
      },
    },
    '/file/list': {
      get: {
        tags: ['file'],
        summary: '获取文件列表',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: '成功' },
        },
      },
    },
    '/file/upload': {
      post: {
        tags: ['file'],
        summary: '上传文件',
        security: [{ bearerAuth: [] }],
        requestBody: {
          content: {
            'multipart/form-data': {
              schema: {
                type: 'object',
                properties: {
                  file: { type: 'string', format: 'binary' },
                },
              },
            },
          },
        },
        responses: {
          200: { description: '上传成功' },
        },
      },
    },
    '/file/download/{docId}': {
      get: {
        tags: ['file'],
        summary: '下载文件',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '成功' },
          404: { description: '文件不存在' },
        },
      },
    },
    '/file/delete/{docId}': {
      delete: {
        tags: ['file'],
        summary: '删除文件',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '删除成功' },
        },
      },
    },
    '/file/new': {
      post: {
        tags: ['file'],
        summary: '新建文件',
        security: [{ bearerAuth: [] }],
        requestBody: {
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  docType: { type: 'string', example: 'docx' },
                  filename: { type: 'string', example: 'new-doc' },
                },
                required: ['docType'],
              },
            },
          },
        },
        responses: {
          200: { description: '创建成功' },
        },
      },
    },
    '/doc/{docId}/meta': {
      get: {
        tags: ['doc'],
        summary: '获取文档元数据',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '成功' },
        },
      },
      put: {
        tags: ['doc'],
        summary: '更新文档元数据',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        requestBody: {
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  name: { type: 'string' },
                  path: { type: 'string' },
                },
              },
            },
          },
        },
        responses: {
          200: { description: '更新成功' },
        },
      },
    },
    '/doc/{docId}/control': {
      get: {
        tags: ['doc'],
        summary: '获取文档控制配置',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '成功' },
        },
      },
      put: {
        tags: ['doc'],
        summary: '更新文档控制配置',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'docId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '更新成功' },
        },
      },
    },
    '/user': {
      get: {
        tags: ['user'],
        summary: '获取所有用户（管理员）',
        security: [{ bearerAuth: [] }],
        responses: {
          200: { description: '成功' },
          403: { description: '无权限' },
        },
      },
    },
    '/user/{userId}': {
      get: {
        tags: ['user'],
        summary: '获取用户信息',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'userId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '成功' },
        },
      },
      put: {
        tags: ['user'],
        summary: '更新用户信息',
        security: [{ bearerAuth: [] }],
        parameters: [
          {
            name: 'userId',
            in: 'path',
            required: true,
            schema: { type: 'string' },
          },
        ],
        responses: {
          200: { description: '更新成功' },
        },
      },
    },
  },
  components: {
    securitySchemes: {
      bearerAuth: {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
      },
    },
  },
};

