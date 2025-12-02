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
 * Document control configuration entity class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doc_control")
public class DocControlEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** User ID */
    @TableField("user_id")
    private String userId;

    /** Document ID */
    @TableField("doc_id")
    private String docId;

    /** Permissions configuration JSON string */
    @TableField("permissions_json")
    private String permissionsJson;

    /** Extension configuration JSON string */
    @TableField("extension_json")
    private String extensionJson;

    /** Watermark configuration JSON string */
    @TableField("watermark_json")
    private String watermarkJson;

    /** Document role */
    @TableField("role")
    private String role;

    /** Creation time */
    @TableField("create_time")
    private Date createTime;

    /** Update time */
    @TableField("update_time")
    private Date updateTime;
}
