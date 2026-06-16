package com.filez.demo.service;

import com.filez.demo.model.DocControl;
import com.filez.demo.model.DocMeta;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DocService {

    /**
     * 根据文件ID下载最新版本文件
     */
    InputStream getDocById(String docId);

    /**
     * 根据文件ID和版本号下载指定版本文件
     * @param docId 文档ID
     * @param version 版本号（对应备份文件的时间戳前缀），"latest" 或为空时返回最新版
     * @return 文件输入流
     */
    InputStream getDocByIdAndVersion(String docId, String version);

    /**
     * 获取指定文档的所有历史版本列表
     * @param docId 文档ID
     * @return 版本号列表（时间戳），按时间倒序排列
     */
    List<String> listVersions(String docId);

    /**
     * 根据文档ID获取文档信息
     */
    DocMeta findDocMetaById(String docId);

    /**
     * 根据文档ID和用户ID获取完整的文档信息（包含用户的控制配置）
     */
    DocMeta findDocMetaWithControlById(String docId, String userId);

    /**
     * 更新文档meta
     */
    DocMeta updateDocMeta(DocMeta docMeta);

    /**
     * 获取文件列表
     */
    List<DocMeta> listFiles();

    /**
     * 上传文件
     */
    DocMeta uploadFile(String docId, InputStream inputStream);
    /**
     * 根据文档ID删除文件
     */
    DocMeta deleteFileByDocId(String docId);

    /**
     * 在指定路径创建空文件
     */
    DocMeta makeNewFile(String name, String path) throws IOException;

    /**
     * 删除指定文档的某个历史版本文件
     * @param docId 文档ID
     * @param version 版本号（时间戳字符串）
     * @return 删除成功返回 true
     */
    boolean deleteVersion(String docId, String version);

    /**
     * 将外部文件保存到本地仓库并注册 DocMeta
     * @param inputStream 文件内容
     * @param fileName 文件名（如 "xxx_平铺文字水印.docx"）
     * @param userId 所属用户ID
     * @return 注册后的 DocMeta（含 docId）
     */
    DocMeta saveExternalFile(InputStream inputStream, String fileName, String userId);

    /**
     * 判断用户是否有权访问对应文件ID
     */
    boolean isAllowedAccess(String docId);

    /**
     * 获取用户控制功能
     */
    DocControl getControl(String userId, String fileId);

    /**
     * 更新用户控制功能
     */
    void updateControl(String userId, String fileId, DocControl controlVO);
}
