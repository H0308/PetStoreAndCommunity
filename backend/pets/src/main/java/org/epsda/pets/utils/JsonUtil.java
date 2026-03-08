package org.epsda.pets.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 12:34
 *
 * @Author: 憨八嘎
 */
@Slf4j
public class JsonUtil {
    // 对象转Json字符串
    public static String toJson(Object o) {
        try {
            return o == null ? null : JSON.toJSONString(o);
        } catch (Exception e) {
            log.error("对象转JSON字符串出现异常，e：{}", e.getMessage());
            return null;
        }
    }

    // Json字符串转对象（支持泛型，如 List<T>）
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            if (typeReference == null || !StringUtils.hasLength(json)) {
                return null;
            }
            return JSON.parseObject(json, typeReference);
        } catch (Exception e) {
            log.error("JSON字符串转对象出现异常，e：{}", e.getMessage());
            return null;
        }
    }

    // Json字符串转对象
    public static <T> T toObject(String json, Class<T> cls) {
        try {
            if (cls == null || !StringUtils.hasLength(json)) {
                return null;
            }
            return JSON.parseObject(json, cls);
        } catch (Exception e) {
            log.error("JSON字符串转对象出现异常，e：{}", e.getMessage());
            return null;
        }
    }
}
