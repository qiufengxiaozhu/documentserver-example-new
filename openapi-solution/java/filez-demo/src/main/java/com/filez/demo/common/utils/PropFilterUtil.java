package com.filez.demo.common.utils;

import com.alibaba.fastjson.serializer.PropertyFilter;
import com.filez.demo.model.DocExtension;
import com.filez.demo.model.DocMeta;
import com.filez.demo.model.DocWaterMark;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

/**
 * 属性过滤器
 */
@Slf4j
public class PropFilterUtil implements PropertyFilter {

    /**
     * FastJSON 属性过滤器实现
     * @param object 当前序列化的对象
     * @param name 对象属性名
     * @param value 属性值
     * @return true:包含该属性(序列化输出) false:忽略该属性(不序列化)
     */
    @Override
    public boolean apply(Object object, String name, Object value) {
        // 如果不是DocMeta对象，默认序列化所有属性
        if (!(object instanceof DocMeta)) {
            return true;
        }

        // 处理extension属性：只有当有追踪修改时才序列化
        if ("extension".equals(name)) {
            DocExtension extension = (DocExtension) value;
            return extension != null &&
                  (extension.isPreviewWithTrackChange() || extension.isTrackChangeForceOn());
        }

        // 处理waterMark属性：只有当有水印内容时才序列化
        if ("waterMark".equals(name)) {
            DocWaterMark waterMark = (DocWaterMark) value;
            return waterMark != null && (Strings.isNotEmpty(waterMark.getLine1()));
        }

        // 默认情况下，忽略filepath属性，其他属性都序列化
        return !"filepath".equals(name);
    }
}

