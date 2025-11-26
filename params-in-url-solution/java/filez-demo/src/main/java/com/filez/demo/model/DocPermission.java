package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document permissions
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

    /** Control whether content can be copied to system clipboard */
    private boolean copy;
}
