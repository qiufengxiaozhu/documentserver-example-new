package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Control special functions of documents
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
     * Specifies the current user's role for the current document in the zoffice editor (for text documents only).
     * This value can be "contributor", "commenter", or "auditor".
     * <p></p>
     * commenter: Annotator; if you need to control a user to only operate annotations in a certain edit,
     * the business system needs to specify in the meta information that the user's role for this edit is annotator.
     * <p></p>
     * auditor: Auditor; if you need to control a user to only view collaboration records,
     * the business system needs to specify in the meta information that the user's role for this edit is auditor.
     */
    private String role;

}
