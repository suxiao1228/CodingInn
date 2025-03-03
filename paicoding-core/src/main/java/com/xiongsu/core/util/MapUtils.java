package com.xiongsu.core.util;

import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUtils {

    public static <K,V>Map<K,V> create(K k, V v, Object... kvs) {
        //先用 Maps.newHashMapWithExpectedSize(kvs.length + 1) 创建一个 HashMap，初始容量是 传入的键值对个数+1。
        //先将第一个键值对 (k, v) 放入 map 中。
        //然后遍历可变参数 kvs，依次把后续的键值对 (K) kvs[i], (V) kvs[i + 1] 加入 map。
        Map<K, V> map = Maps.newHashMapWithExpectedSize(kvs.length + 1);
        map.put(k, v);
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((K) kvs[i], (V) kvs[i + 1]);
        }
        return map;
    }


    //将一个 Collection<T> 转换为 Map<K, V>
    public static <T, K, V> Map<K, V> toMap(Collection<T> list, Function<T, K> key, Function<T, V> val) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMapWithExpectedSize(0);
        }
        return list.stream().collect(Collectors.toMap(key, val));
    }
}
