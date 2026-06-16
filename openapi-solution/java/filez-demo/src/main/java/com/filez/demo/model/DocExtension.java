package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档扩展功能字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocExtension {

    /**
     * true 表示预览时可以显示修订和批注。
     * false 或者不提供该字段表示预览时不显示修订和批注。
     * 仅针对文字文档。
     */
    private boolean previewWithTrackChange;

    /**
     * true 表示编辑时自动打开修订，并且不能关闭。
     * false 或者不提供该字段表示编辑时不强制打开修订。
     * 仅针对文字文档。
     */
    private boolean trackChangeForceOn;

    /**
     * 正常情况下，当文字文档处于保护状态时，可编辑区域会高亮显示。
     * 该值是 true 时，文字文档非保护状态下，可编辑区域也会高亮显示。
     * 仅针对文字文档。
     */
    private boolean showPermMarkForceOn;

    /**
     * 正常情况下，有数据保护的文件，不容许编辑三方系统上传的新版本。
     * 用户在在线编辑器中开启数据保护，并且设置了哪些用户可以编辑哪些区域。
     * 如果用户又在业务系统上传了新版本。因为这些设置不能完全保存到 Office 文档中，
     * 如果zOffice Server 开始编辑新上传的文件，这些在线设置的内容可能会丢失。
     * 该值是 true 时，忽略保护信息，直接编辑三方系统中的新版本。
     */
    private boolean forceNewVersion;
}
