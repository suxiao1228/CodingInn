package com.xiongsu.core.cache;

import com.github.hui.quick.plugin.qrcode.util.json.JsonUtil;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.hui.quick.plugin.qrcode.util.json.JsonUtil.toObj;

public class RedisClient {
    private static final Charset CODE = StandardCharsets.UTF_8;
    private static final String KEY_PREFIX = "pai_";
    private static RedisTemplate<String, String> template;

    public static void register(RedisTemplate<String, String> template) {
        RedisClient.template = template;//初始化template变量
    }

    public static void nullCheck(Object... args) throws IllegalAccessException {  //检查传的对象是否为null
        for(Object obj : args) {
            if(obj == null) {
                throw new IllegalAccessException("redis argument can not be null!");
            }
        }
    }

    /**
     *技术派的缓存值序列化处理
     */
    public static <T> byte[] valBytes(T val) { //将任意类型的对象val转换成字节数组

        if(val instanceof String) {
            return ((String) val).getBytes(CODE);
        }else{
            return JsonUtil.toStr(val).getBytes(CODE);
        }
    }

    /**
     * 生成缓存key
     */
    public static byte[] keyBytes(String key) throws IllegalAccessException {
        nullCheck(key);
        key = KEY_PREFIX + key;
        return key.getBytes(CODE);
    }
    public static byte[][] keyBytes(List<String> keys) throws IllegalAccessException {
        byte[][] bytes = new byte[keys.size()][];
        int index = 0;
        for(String key : keys) {
            bytes[index++] = keyBytes(key);
        }
        return bytes;
    }

    /**
     * 返回key的有效期
     */
    public static Long ttl(String key) {
        return template.execute((RedisCallback<Long>) con -> {
            try {
                return con.ttl(keyBytes(key));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 设置缓存
     */
    public static void setStr(String key, String value) {
        template.execute((RedisCallback<Void>) con -> {
            try {
                con.set(keyBytes(key), valBytes(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
    /**
     * 查询缓存
     */
    public static String getStr(String key) {
        return template.execute((RedisCallback<String>) con -> {
            byte[] val = null;
            try {
                val = con.get(keyBytes(key));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return val == null ? null : new String(val);
        });
    }

    /**
     * 删除缓存
     */
    public static void del(String key) {
        template.execute((RedisCallback<Long>) con -> {
            try {
                return con.del(keyBytes(key));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 设置缓存有效期
     */
    public static void expire(String key, Long expire) {
        template.execute((RedisCallback<Void>) connection -> {
            try {
                connection.expire(keyBytes(key), expire);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    /**
     * 带过期时间的缓存写入
     */
    public static Boolean setStrWithExpire(String key, String value, Long expire) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                try {
                    return redisConnection.setEx(keyBytes(key), expire, valBytes(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public static <T> Map<String, T> hGetAll(String key, Class<T> clz) { //从redis哈希表中获取所有字段
        Map<byte[], byte[]> records = template.execute((RedisCallback<Map<byte[], byte[]>>) con -> {
            try {
                return con.hGetAll(keyBytes(key));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        if (records == null) {
            return Collections.emptyMap();
        }

        Map<String, T> result = Maps.newHashMapWithExpectedSize(records.size());
        for (Map.Entry<byte[], byte[]> entry : records.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            result.put(new String(entry.getKey()), toObj(entry.getValue(), clz));
        }
        return result;
    }

    public static <T> T hGet(String key, String field, Class<T> clz) {//从redis哈希表中获取指定字段的值
        return template.execute((RedisCallback<T>) con -> {
            byte[] records = null;
            try {
                records = con.hGet(keyBytes(key), valBytes(field));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (records == null) {
                return null;
            }

            return toObj(records, clz);
        });
    }

    private static <T> T toObj(byte[] ans, Class<T> clz) {//将字节数组转换为目标T的对象
        if (ans == null) {
            return null;
        }

        if (clz == String.class) {
            return (T) new String(ans, CODE);
        }

        return JsonUtil.toObj(new String(ans, CODE), clz);
    }

    /**
     * 自增
     */
    public static Long hIncr(String key, String filed, Integer cnt) {
        return template.execute((RedisCallback<Long>) con -> {
            try {
                return con.hIncrBy(keyBytes(key), valBytes(filed), cnt);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Boolean hDel(String key, String field) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.hDel(keyBytes(key), valBytes(field)) > 0;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static <T> Boolean hSet(String key, String field, T ans) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                try {
                    return redisConnection.hSet(keyBytes(key), valBytes(field), valBytes(ans));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static <T> void hMSet(String key, Map<String, T> fields) {
        Map<byte[], byte[]> val = Maps.newHashMapWithExpectedSize(fields.size());
        for (Map.Entry<String, T> entry : fields.entrySet()) {
            val.put(valBytes(entry.getKey()), valBytes(entry.getValue()));
        }
        template.execute((RedisCallback<Object>) connection -> {
            try {
                connection.hMSet(keyBytes(key), val);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public static <T> Map<String, T> hMGet(String key, final List<String> fields, Class<T> clz) {
        return template.execute(new RedisCallback<Map<String, T>>() {
            @SneakyThrows
            @Override
            public Map<String, T> doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] f = new byte[fields.size()][];
                IntStream.range(0, fields.size()).forEach(i -> f[i] = valBytes(fields.get(i)));
                List<byte[]> ans = connection.hMGet(keyBytes(key), f);
                Map<String, T> result = Maps.newHashMapWithExpectedSize(fields.size());
                IntStream.range(0, fields.size()).forEach(i -> {
                    result.put(fields.get(i), toObj(ans.get(i), clz));
                });
                return result;
            }
        });
    }

    /**
     * 判断value是否再set中
     *
     * @param key
     * @param value
     * @return
     */
    public static <T> Boolean sIsMember(String key, T value) {
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.sIsMember(keyBytes(key), valBytes(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * 获取set中的所有内容
     *
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> Set<T> sGetAll(String key, Class<T> clz) {
        return template.execute(new RedisCallback<Set<T>>() {
            @Override
            public Set<T> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> set = null;
                try {
                    set = connection.sMembers(keyBytes(key));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (CollectionUtils.isEmpty(set)) {
                    return Collections.emptySet();
                }
                return set.stream().map(s -> toObj(s, clz)).collect(Collectors.toSet());
            }
        });
    }

    /**
     * 往set中添加内容
     *
     * @param key
     * @param val
     * @param <T>
     * @return
     */
    public static <T> boolean sPut(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.sAdd(keyBytes(key), valBytes(val));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }) > 0;
    }

    /**
     * 移除set中的内容
     *
     * @param key
     * @param val
     * @param <T>
     */
    public static <T> void sDel(String key, T val) {
        template.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    connection.sRem(keyBytes(key), valBytes(val));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }


    /**
     * 分数更新
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public static Double zIncrBy(String key, String value, Integer score) {
        return template.execute(new RedisCallback<Double>() {
            @Override
            public Double doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.zIncrBy(keyBytes(key), score, valBytes(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static ImmutablePair<Integer, Double> zRankInfo(String key, String value) {
        double score = zScore(key, value);
        int rank = zRank(key, value);
        return ImmutablePair.of(rank, score);
    }

    /**
     * 获取分数
     *
     * @param key
     * @param value
     * @return
     */
    public static Double zScore(String key, String value) {
        return template.execute(new RedisCallback<Double>() {
            @Override
            public Double doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.zScore(keyBytes(key), valBytes(value));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static Integer zRank(String key, String value) {
        return template.execute(new RedisCallback<Integer>() {
            @Override
            public Integer doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.zRank(keyBytes(key), valBytes(value)).intValue();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * 找出排名靠前的n个
     *
     * @param key
     * @param n
     * @return
     */
    public static List<ImmutablePair<String, Double>> zTopNScore(String key, int n) {
        return template.execute(new RedisCallback<List<ImmutablePair<String, Double>>>() {
            @Override
            public List<ImmutablePair<String, Double>> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<RedisZSetCommands.Tuple> set = null;
                try {
                    set = connection.zRangeWithScores(keyBytes(key), -n, -1);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (set == null) {
                    return Collections.emptyList();
                }
                return set.stream()
                        .map(tuple -> ImmutablePair.of(toObj(tuple.getValue(), String.class), tuple.getScore()))
                        .sorted((o1, o2) -> Double.compare(o2.getRight(), o1.getRight())).collect(Collectors.toList());
            }
        });
    }


    public static <T> Long lPush(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.lPush(keyBytes(key), valBytes(val));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static <T> Long rPush(String key, T val) {
        return template.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    return connection.rPush(keyBytes(key), valBytes(val));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static <T> List<T> lRange(String key, int start, int size, Class<T> clz) {
        return template.execute(new RedisCallback<List<T>>() {

            @Override
            public List<T> doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> list = null;
                try {
                    list = connection.lRange(keyBytes(key), start, size);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (CollectionUtils.isEmpty(list)) {
                    return new ArrayList<>();
                }
                return list.stream().map(k -> toObj(k, clz)).collect(Collectors.toList());
            }
        });
    }

    public static void lTrim(String key, int start, int size) {
        template.execute(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    connection.lTrim(keyBytes(key), start, size);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }


    public static PipelineAction pipelineAction() {
        return new PipelineAction();
    }

    /**
     * redis 管道执行的封装链路
     */
    public static class PipelineAction { //允许多个Redis命令批量发送
        private List<Runnable> run = new ArrayList<>();

        private RedisConnection connection;

        public PipelineAction add(String key, BiConsumer<RedisConnection, byte[]> conn) {
            run.add(() -> {
                try {
                    conn.accept(connection, RedisClient.keyBytes(key));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            return this;
        }

        public PipelineAction add(String key, String field, ThreeConsumer<RedisConnection, byte[], byte[]> conn) {
            run.add(() -> {
                try {
                    conn.accept(connection, RedisClient.keyBytes(key), valBytes(field));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            return this;
        }

        public void execute() {
            template.executePipelined((RedisCallback<Object>) connection -> {
                PipelineAction.this.connection = connection;
                run.forEach(Runnable::run);
                return null;
            });
        }
    }

    @FunctionalInterface
    public interface ThreeConsumer<T, U, P> {
        void accept(T t, U u, P p);
    }
}
//为什么要转换成byte[]
//原因：
//
//Redis 底层操作 API 只支持 byte[]
//
//RedisConnection.setEx()、hDel() 等方法需要 byte[] 作为 key 和 value。
//RedisTemplate 默认使用 StringRedisSerializer 处理 String，而 RedisConnection 需要手动转换。
//避免编码问题
//
//String 直接存 Redis 可能因字符集不同（如 UTF-8、GBK）导致读取错误。
//byte[] 方式显式指定编码（如 UTF-8），保证一致性。
//提高 Redis 性能
//
//byte[] 方式避免 String -> byte[] 多次转换，减少 CPU 和 GC 压力。