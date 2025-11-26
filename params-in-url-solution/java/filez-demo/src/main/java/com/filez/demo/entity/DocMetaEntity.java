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
 * Document metadata entity class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doc_meta")
public class DocMetaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Document ID, primary key */
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /** Filename, must include file extension */
    @TableField("name")
    private String name;

    /** Optional, file description information */
    @TableField("description")
    private String description;

    /** Optional, file creation time */
    @TableField("created_at")
    private Date createdAt;

    /** Required, file modification time */
    @TableField("modified_at")
    private Date modifiedAt;

    /** File size, required */
    @TableField("size")
    private Long size;

    /** Optional, version */
    @TableField("version")
    private String version;

    /** File path */
    @TableField("filepath")
    private String filepath;

    /** Document role */
    @TableField("role")
    private String role;

    /** Creator ID */
    @TableField("created_by_id")
    private String createdById;

    /** Creator name */
    @TableField("created_by_name")
    private String createdByName;

    /** Creator email */
    @TableField("created_by_email")
    private String createdByEmail;

    /** Modifier ID */
    @TableField("modified_by_id")
    private String modifiedById;

    /** Modifier name */
    @TableField("modified_by_name")
    private String modifiedByName;

    /** Modifier email */
    @TableField("modified_by_email")
    private String modifiedByEmail;

    /** Owner ID */
    @TableField("owner_id")
    private String ownerId;

    /** Owner name */
    @TableField("owner_name")
    private String ownerName;

    /** Owner email */
    @TableField("owner_email")
    private String ownerEmail;


    /** Creation time */
    @TableField("create_time")
    private Date createTime;

    /** Update time */
    @TableField("update_time")
    private Date updateTime;
}
