package com.bytearch.common.redis.facade;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author iyw
 */
public interface RedisSupport {
    /**
     * 设置缓存对象，永久有效
     *
     * @param key
     * @param value
     * @return
     */
    @Deprecated
    String set(String key, Object value);
    /**
     * 设置缓存对象
     *
     * @param key
     * @param value
     * @param expTime
     * @return
     */
    String set(final String key, final int expTime, final Object value);

    /**
     * 设置缓存对象
     *
     * @param key
     * @param expTime
     * @param value
     * @return
     */
    String set(final String key, final int expTime, final String value);

    boolean set(final String key, final Object value, final String nxxx, final String expx, final long time);

    /**
     * 将key设置值为value，如果key不存在，这种情况下等同SET命令。
     * 当key存在时，什么也不做。
     * @param key
     * @param value
     * @return
     *  1 如果key被设置了
     *  0 如果key没有被设置
     */
    Long setnx(final String key, final Object value);

    /**
     * 获取缓存对象，不支持list，map
     *
     * @param key
     * @param classType
     * @param <T>
     * @return
     */
    <T> T get(String key, Class<T> classType);

    /**
     * 获取缓存对象
     *
     * @param key
     * @return
     */
    String get(final String key);

    /**
     * 删除缓存对象
     *
     * @param key
     * @return
     */
    long delete(final String key);

    long hincrBy(final String key, final String field, final long value);

    byte[] hget(final String key, final String field);

    /**
     * @param key
     * @param delta 累加值，非负值
     * @return
     */
    long incrBy(final String key, final long delta);

    long rpush(final String key, final Object value);

    <T> T lpop(final String key, Class<T> javaType);

    <T> T blpop(final String key, final int timeout, Class<T> javaType);

    <T> List<T> lrange(final String key, int start, int end);

    long expire(final String key, final int seconds);

    Boolean exists(final String key);

    /** 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。*/
    long hdel(String key, String... field);

    /** 返回哈希表 key 中，所有的域和值*/
    Map<String, Object> hgetAll(String key);

    /** 同时将多个 field-value (域-值)对设置到哈希表 key 中 */
    String hmset(String key, Map<String, Object> map);

    /** 返回哈希表 key 中，一个或多个给定域的值 */
    List<Object> hmget(String key, String... field);

    /** 返回哈希表 key 中所有域的值 */
    <T> List<T> hvals(String key);

    /** 返回哈希表 key 中的所有域 */
    Set<String> hkeys(String key);

    <T> Set<T> smembers4Sets(final String key, Class<T> javaType);

    <T> T getSet(final String key, final Object value, Class<T> javaType);

    Long sadd4Sets(final String key, final Object ... values);

    Long srem4Sets(final String key, final Object... values);

    /**
     * 返回存储在 key 里的list的长度。
     * 如果 key 不存在，那么就被看作是空list，并且返回长度为 0。
     * @param key
     * @return
     */
    Long llen(String key);

    /**
     * 从存于 key 的列表里移除前 count 次出现的值为 value 的元素。
     * 如果list里没有存在key就会被当作空list处理，所以当 key 不存在的时候，这个命令会返回 0。
     * @param key
     * @param count
     * @param value
     * @return
     */
    Long lrem(String key, long count, Object value);
}
