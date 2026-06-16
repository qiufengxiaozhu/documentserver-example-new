package com.filez.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * OpenAPI 通用任务实体类
 * 支持水印、转换、合并、拆分等所有 OpenAPI 异步任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("openapi_task")
public class OpenApiTaskEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** luoshu-server 返回的任务ID */
    @TableField("task_id")
    private String taskId;

    /** 调用的 API 接口名称，如 content/update、convert 等 */
    @TableField("api_name")
    private String apiName;

    /** 文档ID */
    @TableField("doc_id")
    private String docId;

    /** 文档名称 */
    @TableField("doc_name")
    private String docName;

    /** 操作类型：ApplyWatermark / ApplyPicWatermark / ApplyWatermarkForFixed / convert 等 */
    @TableField("op_type")
    private String opType;

    /** 提交到 server 的完整请求体 JSON */
    @TableField("request_body")
    private String requestBody;

    /** 任务状态：IN_QUEUE / PROCESSING / SUCCESS / FAIL */
    @TableField("status")
    private String status;

    /** 任务成功后的 contentId，用于下载结果 */
    @TableField("content_id")
    private String contentId;

    /** 结果文件名 */
    @TableField("result_filename")
    private String resultFilename;

    /** 结果文件注册到仓库后的 docId，用于预览 */
    @TableField("result_doc_id")
    private String resultDocId;

    /** server 原样返回的错误信息 */
    @TableField("error_msg")
    private String errorMsg;

    /** server 原样返回的完整响应体 */
    @TableField("server_response")
    private String serverResponse;

    /** 用户ID */
    @TableField("user_id")
    private String userId;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
