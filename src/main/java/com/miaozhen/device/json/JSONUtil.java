package com.miaozhen.device.json;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    private static final Log log = LogFactory.getLog(JSONUtil.class);
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        /**
         * 设置序列化配置，为null的属性不加入到json中
         */
        mapper.setSerializationInclusion(Include.NON_NULL);
        /**
         * 兼容单引号但单引号不属于json标准不建议使用
         */
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static String write2JsonStr(Object o) {
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("write2JsonStr() exception: " + e.getMessage());
        }
        return jsonStr;
    }

    public static Object json2Object(String json, Class<?> clazz) {
        try {
            if (json != null) {
                return mapper.readValue(json, clazz);
            }
        } catch (JsonParseException e) {
            log.error("json2Object() parseException: " + e.getMessage());
        } catch (JsonMappingException e) {
            log.error("json2Object() mappingException: " + e.getMessage());
        } catch (IOException e) {
            log.error("json2Object() IOException: " + e.getMessage());
        }
        return null;
    }
}