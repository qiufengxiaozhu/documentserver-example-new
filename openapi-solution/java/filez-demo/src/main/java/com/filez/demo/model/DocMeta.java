package com.filez.demo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.filez.demo.common.utils.DateToUtcZUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocMeta {

    /** 文档ID */
    @JSONField(ordinal = 1)
    private String id;

    /** 文件名，必须包含文件的扩展名 */
    @JSONField(ordinal = 2)
    private String name;

    /** 可选，文件描述信息 */
    private String description;

    /** 可选，文件创建时间，格式必须为：2020-03-25T02:57:38.000Z */
    @JSONField(name = "created_at", serializeUsing = DateToUtcZUtil.class)
    @Builder.Default
    private Date createdAt = new Date(870019200000L);

    /** 必选，文件创建人信息 */
    @JSONField(ordinal = 4, name = "created_by")
    private Profile createdBy;

    /** 必选，文件修改时间 */
    @JSONField(ordinal = 3, name = "modified_at", serializeUsing = DateToUtcZUtil.class)
    private Date modifiedAt;

    /** 可选，文件修改人信息，如果没有，zOffice 编辑器自动用 created_by 作为 modified_by */
    @JSONField(name = "modified_by")
    private Profile modifiedBy;

    /** 必选 */
    @JSONField(ordinal = 5)
    private DocPermission permissions;

    /** 可选 */
    private DocExtension extension;

    /** 可选 */
    private DocWaterMark waterMark;

    /** 可选，如果没有，zOffice 编辑器自动用 created_by 作为 owner */
    private Profile owner;

    /** 文件大小，必填 */
    private Long size;

    /** 可选 */
    private String version;

    private String filepath;

    /** (仅针对文字文档)。该项值可以是"contributor"，"commenter"，"auditor" */
    private String role;

    public DocMeta(@NonNull String id, @NonNull String name, Profile createdBy, @NonNull Date modifiedAt, @NonNull DocPermission permissions) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.permissions = permissions;
    }
}
