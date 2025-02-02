package com.xiongsu.core.senstive.ibatis;

import com.xiongsu.core.senstive.ano.SensitiveField;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 敏感词相关配置，db配置表中的配置优先级更高，支持动态刷新
 *
 * @author YiHui
 * @date 2023/8/9
 */
@Data
public class SensitiveObjectMeta {
    private static final String JAVA_LANG_OBJECT = "java.lang.object";
    /**
     * 是否启用脱敏
     */
    private Boolean enabledSensitiveReplace;

    /**
     * 类名
     */
    private String className;

    /**
     * 标注 SensitiveField 的成员
     */
    private List<SensitiveFieldMeta> sensitiveFieldMetaList;

    public static Optional<SensitiveObjectMeta> buildSensitiveObjectMeta(Object param) {//用于根据传入的对象 param 创建并返回 SensitiveObjectMeta 实例。
        if (isNull(param)) {
            return Optional.empty();
        }

        Class<?> clazz = param.getClass();
        SensitiveObjectMeta sensitiveObjectMeta = new SensitiveObjectMeta();
        sensitiveObjectMeta.setClassName(clazz.getName());

        List<SensitiveFieldMeta> sensitiveFieldMetaList = newArrayList();
        sensitiveObjectMeta.setSensitiveFieldMetaList(sensitiveFieldMetaList);
        boolean sensitiveField = parseAllSensitiveFields(clazz, sensitiveFieldMetaList);
        sensitiveObjectMeta.setEnabledSensitiveReplace(sensitiveField);
        return Optional.of(sensitiveObjectMeta);
    }


    private static boolean parseAllSensitiveFields(Class<?> clazz, List<SensitiveFieldMeta> sensitiveFieldMetaList) {//用于递归查找指定类（及其父类）中的敏感字段。
        Class<?> tempClazz = clazz;
        boolean hasSensitiveField = false;
        while (nonNull(tempClazz) && !JAVA_LANG_OBJECT.equalsIgnoreCase(tempClazz.getName())) {
            for (Field field : tempClazz.getDeclaredFields()) {
                SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                if (nonNull(sensitiveField)) {
                    SensitiveFieldMeta sensitiveFieldMeta = new SensitiveFieldMeta();
                    sensitiveFieldMeta.setName(field.getName());
                    sensitiveFieldMeta.setBindField(sensitiveField.bind());
                    sensitiveFieldMetaList.add(sensitiveFieldMeta);
                    hasSensitiveField = true;
                }
            }
            tempClazz = tempClazz.getSuperclass();
        }
        return hasSensitiveField;
    }


    @Data
    public static class SensitiveFieldMeta {//这是一个静态嵌套类，用于存储敏感字段的元数据，包括字段名称（name）和绑定的数据库字段别名（bindField）。
        /**
         * 默认根据字段名，找db中同名的字段
         */
        private String name;

        /**
         * 绑定的数据库字段别名
         */
        private String bindField;
    }
}
