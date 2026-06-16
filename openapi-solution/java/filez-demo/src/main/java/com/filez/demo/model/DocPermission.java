package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档权限
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocPermission {

    @Builder.Default
    private boolean write = true;

    @Builder.Default
    private boolean read = true;

    private boolean download;

    private boolean print;

    /** 控制内容是否可以复制到系统剪贴版 */
    private boolean copy;
}
