package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 控制文档的特殊功能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocControl {

    private DocPermission docPermission;

    private DocWaterMark docWaterMark;

    private DocExtension extension;

    /**
     * 指明当前用户对当前文档在 zoffice 编辑器里的角色(仅针对文字文档)。
     * 该项值可以是"contributor"，"commenter"，"auditor"。
     * <p></p>
     * commenter：批注者；如果需要控制某个用户某次编辑只能操作批注，需要业务
     * 系统在 meta 信息中指定，本次编辑用户的角色是批注者。
     * <p></p>
     * auditor：审计者；如果需要控制某个用户只能查看协作记录，需要业务系统
     * 在 meta 信息中指定，本次编辑用户的角色是审计者。
     */
    private String role;

}
