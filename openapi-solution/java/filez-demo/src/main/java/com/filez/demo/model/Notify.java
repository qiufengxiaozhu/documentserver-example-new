package com.filez.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notify {

    private String docId;

    private String repoId;

    private String type;
}
