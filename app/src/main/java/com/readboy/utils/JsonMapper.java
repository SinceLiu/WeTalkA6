package com.readboy.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by oubin on 2017/9/21.
 */

public class JsonMapper {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // 设置输出时包含属性的风格
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private JsonMapper() {
        this(JsonInclude.Include.NON_EMPTY);
    }

    public JsonMapper(JsonInclude.Include include) {
        mapper = new ObjectMapper();
        // 设置输出时包含属性的风格
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
     */
    public static JsonMapper nonEmptyMapper() {
        return new JsonMapper(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * 创建只输出初始值被改变的属性到Json字符串的Mapper, 最节约的存储方式，建议在内部接口中使用。
     */
    public static JsonMapper nonDefaultMapper() {
        return new JsonMapper(JsonInclude.Include.NON_DEFAULT);
    }

    /**
     * Object可以是POJO，也可以是Collection或数组。
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     */
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 反序列化POJO或简单Collection如List<String>.
     * <p>
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p>
     * 如需反序列化复杂Collection如List<MyBean>, 请使用fromJson(String, JavaType)
     *
     * @see #fromJson(String, JavaType)
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 反序列化复杂Collection如List<Bean>, 先使用createCollectionType()或contructMapType()构造类型, 然后调用本函数.
     */
    @SuppressWarnings("unchecked")
    public static  <T> T fromJson(String jsonString, JavaType javaType) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> List<T> fromJsonArray(String jsonString, Class<T> tClass) {
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, new TypeReference<List<T>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构造Collection类型.
     */
    @SuppressWarnings("rawtypes")
    public static JavaType contructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    /**
     * 构造Map类型.
     */
    @SuppressWarnings("rawtypes")
    public static JavaType contructMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

    /**
     * 当JSON里只含有Bean的部分屬性時，更新一個已存在Bean，只覆蓋該部分的屬性.
     */
    public static void update(String jsonString, Object object) {
        try {
            mapper.readerForUpdating(object).readValue(jsonString);
        } catch (IOException e) {
            Log.e("JsonMapper", "update: IOException: " + e.toString(), e);
        }
    }

    /**
     * 輸出JSONP格式數據.
     */
    public String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }

    /**
     * 設定是否使用Enum的toString函數來讀寫Enum,
     * 為False時時使用Enum的name()函數來讀寫Enum, 默認為False.
     * 注意本函數一定要在Mapper創建後, 所有的讀寫動作之前調用.
     */
    public static void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }


    /**
     * 取出Mapper做进一步的设置或使用其他序列化API.
     */
    public ObjectMapper getMapper() {
        return mapper;
    }


}
