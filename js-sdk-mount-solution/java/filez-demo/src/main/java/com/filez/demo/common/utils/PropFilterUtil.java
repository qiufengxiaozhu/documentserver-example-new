package com.filez.demo.common.utils;

import com.alibaba.fastjson.serializer.PropertyFilter;
import com.filez.demo.model.DocExtension;
import com.filez.demo.model.DocMeta;
import com.filez.demo.model.DocWaterMark;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

/**
 * Property filter
 */
@Slf4j
public class PropFilterUtil implements PropertyFilter {

    /**
     * FastJSON property filter implementation
     * @param object Current object being serialized
     * @param name Object property name
     * @param value Property value
     * @return true: include this property (serialize output) false: ignore this property (don't serialize)
     */
    @Override
    public boolean apply(Object object, String name, Object value) {
        // If not a DocMeta object, serialize all properties by default
        if (!(object instanceof DocMeta)) {
            return true;
        }

        // Handle extension property: only serialize when there are tracked changes
        if ("extension".equals(name)) {
            DocExtension extension = (DocExtension) value;
            return extension != null &&
                  (extension.isPreviewWithTrackChange() || extension.isTrackChangeForceOn());
        }

        // Handle waterMark property: serialize only when there is watermark content
        if ("waterMark".equals(name)) {
            DocWaterMark waterMark = (DocWaterMark) value;
            return waterMark != null && (Strings.isNotEmpty(waterMark.getLine1()));
        }

        // By default, ignore filepath property, serialize all other properties
        return !"filepath".equals(name);
    }
}

