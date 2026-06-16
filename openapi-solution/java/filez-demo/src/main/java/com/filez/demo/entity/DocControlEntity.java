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
 * 文档控制配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doc_control")
public class DocControlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private String userId;

    /** 文档ID */
    @TableField("doc_id")
    private String docId;

    /** 权限配置JSON字符串 */
    @TableField("permissions_json")
    private String permissionsJson;

    /** 扩展配置JSON字符串 */
    @TableField("extension_json")
    private String extensionJson;

    /** 水印配置JSON字符串 */
    @TableField("watermark_json")
    private String watermarkJson;

    /** 文档角色 */
    @TableField("role")
    private String role;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
