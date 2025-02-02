package com.xiongsu.core.senstive.ibatis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 敏感词缓存
 *
 * @author YiHui
 * @date 2023/8/9
 */
public class SensitiveMetaCache {
    private static ConcurrentHashMap<String, SensitiveObjectMeta> CACHE = new ConcurrentHashMap<>();

    public static SensitiveObjectMeta get(String key) {
        return CACHE.get(key);
    }//获取缓存值

    public static void put(String key, SensitiveObjectMeta meta) {
        CACHE.put(key, meta);
    }//存储缓存值

    public static void remove(String key) {
        CACHE.remove(key);
    }//移除缓存值

    public static boolean contains(String key) {
        return CACHE.containsKey(key);
    }//检查缓存中是否包含某个键

    public static SensitiveObjectMeta putIfAbsent(String key, SensitiveObjectMeta meta) {
        return CACHE.putIfAbsent(key, meta);
    }//如果键不存在则插入缓存

    public static SensitiveObjectMeta computeIfAbsent(String key, Function<String, SensitiveObjectMeta> function) {
        return CACHE.computeIfAbsent(key, function);
    }//如果键不存在，则根据函数计算后插入缓存
}
