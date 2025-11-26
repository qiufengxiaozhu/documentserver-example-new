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

    /** Document ID */
    @JSONField(ordinal = 1)
    private String id;

    /** Filename, must include file extension */
    @JSONField(ordinal = 2)
    private String name;

    /** Optional, file description information */
    private String description;

    /** Optional, file creation time, format must be: 2020-03-25T02:57:38.000Z */
    @JSONField(name = "created_at", serializeUsing = DateToUtcZUtil.class)
    @Builder.Default
    private Date createdAt = new Date(870019200000L);

    /** Required, file creator information */
    @JSONField(ordinal = 4, name = "created_by")
    private Profile createdBy;

    /** Required, file modification time */
    @JSONField(ordinal = 3, name = "modified_at", serializeUsing = DateToUtcZUtil.class)
    private Date modifiedAt;

    /** Optional, file modifier information, if not provided, zOffice editor automatically uses created_by as modified_by */
    @JSONField(name = "modified_by")
    private Profile modifiedBy;

    /** Required */
    @JSONField(ordinal = 5)
    private DocPermission permissions;

    /** Optional */
    private DocExtension extension;

    /** Optional */
    private DocWaterMark waterMark;

    /** Optional, if not provided, zOffice editor automatically uses created_by as owner */
    private Profile owner;

    /** File size, required */
    private Long size;

    /** Optional */
    private String version;

    private String filepath;

    /** (For text documents only). This value can be "contributor", "commenter", "auditor" */
    private String role;

    public DocMeta(@NonNull String id, @NonNull String name, Profile createdBy, @NonNull Date modifiedAt, @NonNull DocPermission permissions) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.permissions = permissions;
    }
}
