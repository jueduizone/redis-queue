package com.jamie.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static final String REDIS_IP = "127.0.0.1";
    private static final Integer REDIS_PORT = 6379;
    static JedisPool pool = null;

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setMaxTotal(20);
        pool = new JedisPool(jedisPoolConfig, REDIS_IP, REDIS_PORT);
    }

    public static Jedis getJedis() {
        try {
            Jedis jedis = pool.getResource();
            return jedis;
        } catch (Exception e) {
            logger.info("获取redis异常");
        }
        return null;
    }

    /**
     * redis 放入对象
     *
     * @param key
     * @param value
     */
    public static void setObject(String key, Object value) {

    }


}
