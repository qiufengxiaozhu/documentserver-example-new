package com.filez.demo.model;

import lombok.Data;

import java.util.List;

/**
 * 批注信息
 */
@Data
public class Mention {

    private String type;

    private String content;

    private String link;

    private String owner;

    private String author;

    private String commentsid;

    private String fileid;

    private String filename;

    private List<String> mentionList;


}
