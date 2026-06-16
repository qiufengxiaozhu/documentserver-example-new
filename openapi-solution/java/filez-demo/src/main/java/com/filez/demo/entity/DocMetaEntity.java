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
 * 文档元数据实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doc_meta")
public class DocMetaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档ID，主键 */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** 文件名，必须包含文件的扩展名 */
    @TableField("name")
    private String name;

    /** 可选，文件描述信息 */
    @TableField("description")
    private String description;

    /** 可选，文件创建时间 */
    @TableField("created_at")
    private Date createdAt;

    /** 必选，文件修改时间 */
    @TableField("modified_at")
    private Date modifiedAt;

    /** 文件大小，必填 */
    @TableField("size")
    private Long size;

    /** 可选，版本 */
    @TableField("version")
    private String version;

    /** 文件路径 */
    @TableField("filepath")
    private String filepath;

    /** 文档角色 */
    @TableField("role")
    private String role;

    /** 创建人ID */
    @TableField("created_by_id")
    private String createdById;

    /** 创建人姓名 */
    @TableField("created_by_name")
    private String createdByName;

    /** 创建人邮箱 */
    @TableField("created_by_email")
    private String createdByEmail;

    /** 修改人ID */
    @TableField("modified_by_id")
    private String modifiedById;

    /** 修改人姓名 */
    @TableField("modified_by_name")
    private String modifiedByName;

    /** 修改人邮箱 */
    @TableField("modified_by_email")
    private String modifiedByEmail;

    /** 拥有者ID */
    @TableField("owner_id")
    private String ownerId;

    /** 拥有者姓名 */
    @TableField("owner_name")
    private String ownerName;

    /** 拥有者邮箱 */
    @TableField("owner_email")
    private String ownerEmail;


    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;
}
