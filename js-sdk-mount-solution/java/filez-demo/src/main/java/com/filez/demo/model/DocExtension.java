package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document extension function fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocExtension {

    /**
     * true means revisions and comments can be displayed during preview.
     * false or not providing this field means revisions and comments are not displayed during preview.
     * Only for text documents.
     */
    private boolean previewWithTrackChange;

    /**
     * true means automatically turn on revision tracking during editing and cannot be turned off.
     * false or not providing this field means revision tracking is not forced on during editing.
     * Only for text documents.
     */
    private boolean trackChangeForceOn;

    /**
     * Normally, when text documents are in protected state, editable areas are highlighted.
     * When this value is true, editable areas are also highlighted when text documents are not in protected state.
     * Only for text documents.
     */
    private boolean showPermMarkForceOn;

    /**
     * Normally, files with data protection do not allow editing of new versions uploaded by third-party systems.
     * Users enable data protection in the online editor and set which users can edit which areas.
     * If users upload a new version in the business system, these settings cannot be fully saved to Office documents.
     * If zOffice Server starts editing newly uploaded files, these online settings may be lost.
     * When this value is true, protection information is ignored and new versions in third-party systems are edited directly.
     */
    private boolean forceNewVersion;
}
