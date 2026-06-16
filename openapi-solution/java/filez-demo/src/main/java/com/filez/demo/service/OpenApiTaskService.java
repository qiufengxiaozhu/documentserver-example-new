package com.filez.demo.service;

import com.filez.demo.entity.OpenApiTaskEntity;

import java.util.List;
import java.util.Map;

/**
 * OpenAPI 通用任务服务接口
 * 负责对接 luoshu-server 的各种 PublicAPI 接口
 */
public interface OpenApiTaskService {

    /**
     * 提交水印任务到 luoshu-server
     * @param docId 文档ID
     * @param docName 文档名称
     * @param opType 操作类型（水印四种类型之一）
     * @param paramsJson 前端传入的 ops 数组 JSON
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token（server 回调 demo 接口时使用）
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitWatermarkTask(String docId, String docName, String opType,
                                             String paramsJson, String userId, String userToken);

    /**
     * 查询任务状态（代理 luoshu-server 的 queryTaskStatus 接口）
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> queryTaskStatus(String taskId);

    /**
     * 分页查询任务列表
     * @param userId 用户ID
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Map<String, Object> getTaskList(String userId, int pageNo, int pageSize);

    /**
     * 删除单个任务
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(String taskId);

    /**
     * 批量删除任务
     * @param taskIds 任务ID列表
     * @return 删除数量
     */
    int batchDeleteTasks(List<String> taskIds);

    /**
     * 根据 taskId 获取任务详情
     * @param taskId 任务ID
     * @return 任务实体
     */
    OpenApiTaskEntity getTaskByTaskId(String taskId);

    /**
     * 更新任务实体
     * @param entity 任务实体
     */
    void updateTask(OpenApiTaskEntity entity);

    /**
     * 下载任务结果文件到本地仓库并注册为 DocMeta
     * @param taskId 任务ID
     * @param contentId 结果 contentId
     * @return 注册后的 docId，失败返回 null
     */
    String downloadAndRegisterResult(String taskId, String contentId);

    /**
     * 获取任务结果文件在本地仓库中注册的 docId
     * @param taskId 任务ID
     * @return docId，未注册则返回 null
     */
    String getResultDocId(String taskId);

    /**
     * 构造 PublicAPI content/update 的完整请求体
     * @param docId 文档ID
     * @param docName 文档名称
     * @param opsJson ops 数组的 JSON 字符串
     * @param userToken 当前用户的 JWT token（server 回调 demo 时携带）
     * @return 完整请求体 JSON
     */
    String buildRequestBody(String docId, String docName, String opsJson, String userToken);

    /**
     * 提交格式转换任务到 luoshu-server
     * @param docId 源文档ID
     * @param docName 源文档名称（含扩展名）
     * @param targetFilename 目标文件名（含扩展名）
     * @param watermarkJson 水印配置 JSON（可选，null 表示不加水印）
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitConvertTask(String docId, String docName, String targetFilename,
                                           String watermarkJson, String userId, String userToken);

    /**
     * 提交图片转 PDF 任务到 luoshu-server
     * @param docId 源图片文档ID
     * @param docName 源图片文件名（含扩展名）
     * @param pageSize 页面尺寸：FIT_IMAGE / A4 / A3
     * @param orientation 页面方向：auto / portrait / landscape（FIT_IMAGE 时忽略）
     * @param margin 页边距：none / narrow / wide（FIT_IMAGE 时忽略）
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitImg2PdfTask(String docId, String docName, String pageSize,
                                           String orientation, String margin,
                                           String userId, String userToken);

    /**
     * 提交文档拆分任务到 luoshu-server（支持 PDF 和 Word）
     * @param docId 源文档ID
     * @param docName 源文件名（含扩展名，支持 .pdf/.doc/.docx/.wps）
     * @param type 拆分类型：PDF 时为 PAGERANGE/FIXEDPAGES/FILECOUNT；Word 时为 WORDHEADING/SECTBREAK/TEXT
     * @param ranges 页码范围表达式（PDF PAGERANGE 时必填）
     * @param fixedPages 每份固定页数（PDF FIXEDPAGES 时必填）
     * @param fileCount 拆分为几份（PDF FILECOUNT 时必填）
     * @param output 输出模式：singleFile / array（仅 PDF PAGERANGE 时生效）
     * @param keyword 拆分关键字（Word TEXT 时必填）
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitDocSplitTask(String docId, String docName, String type,
                                            String ranges, String fixedPages, String fileCount,
                                            String output, String keyword,
                                            String userId, String userToken);

    /**
     * 提交 JSON 转 Excel 任务到 luoshu-server
     * @param docId 源 JSON 文档ID
     * @param docName 源 JSON 文件名（含扩展名）
     * @param sheetName 工作表名称（可选），null 表示不传（服务端使用 "Sheet1"）
     * @param columnOrder 列顺序（可选），逗号分隔的列名，null 表示不传（按 JSON key 顺序）
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitJson2ExcelTask(String docId, String docName,
                                              String sheetName, String columnOrder,
                                              String userId, String userToken);

    /**
     * 提交 PPT 加水印任务（串联：先转换为 PDF，再在 PDF 上加水印）
     * @param docId 源 PPT 文档ID
     * @param docName 源 PPT 文件名（含扩展名）
     * @param watermarkType 水印类型 actId
     * @param opsJson 水印参数 ops 数组 JSON
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含最终水印任务 taskId 的结果
     */
    Map<String, Object> submitPptWatermarkTask(String docId, String docName, String watermarkType,
                                                String opsJson, String userId, String userToken);

    /**
     * 提交文档套红（书签内容替换）任务到 luoshu-server
     * 底层通过 content/update 接口的 UpdateBookmarkRef 操作实现
     * @param docId 模板文档ID
     * @param docName 模板文档名称（含扩展名，doc/docx/wps）
     * @param opsJson 套红参数 ops 数组 JSON（actId 为 UpdateBookmarkRef）
     * @param userId 当前用户ID
     * @param userToken 当前用户的 JWT token
     * @return 包含 taskId 和状态的结果
     */
    Map<String, Object> submitBookmarkTask(String docId, String docName,
                                            String opsJson, String userId, String userToken);
}
