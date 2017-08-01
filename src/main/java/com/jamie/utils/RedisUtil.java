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
    private static final String REDIS_PREIFX = "Delay_Queue_";
    public static final String REDIS_JOB_POOL = "Job_Pool";
    public static final String REDIS_JOB_READY = "Ready_Queue";
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

    public static void delObject(String key) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            jedis.del((REDIS_PREIFX + key).getBytes());
        } catch (Exception e) {
            logger.info("设置属性异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * redis 放入对象
     *
     * @param key
     * @param value
     */
    public static Boolean setObject(String key, Object value) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            byte[] byteVal = KryoUtil.serialize(value);
            jedis.set((REDIS_PREIFX + key).getBytes(), byteVal);
            return true;
        } catch (Exception e) {
            logger.info("设置属性异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * redis 获取对象
     *
     * @param key
     * @return
     */
    public static Object getObject(String key) {
        Jedis jedis = RedisUtil.getJedis();
        Object o = null;
        try {
            byte[] byteVal = jedis.get((REDIS_PREIFX + key).getBytes());
            o = KryoUtil.deserialize(byteVal);
        } catch (Exception e) {
            logger.info("设置属性异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return o;
    }

    /**
     * 将值放入队列
     *
     * @param value
     * @return
     */
    public static Boolean lpush(String key, String value) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            jedis.lpush(REDIS_PREIFX + key, value);
            return true;
        } catch (Exception e) {
            logger.info("redis 放入 list 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * 值从list按先进先出处理
     *
     * @param key
     * @return
     */
    public static Object rpop(String key) {
        Jedis jedis = RedisUtil.getJedis();
        Object o = null;
        try {
            //取出列表的最后一个元素，先进先出
            byte[] byteVal = jedis.rpop((REDIS_PREIFX + key).getBytes());
            if (byteVal != null) {
                o = KryoUtil.deserialize(byteVal);
            }
        } catch (Exception e) {
            logger.info("redis 取出 list 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return o;
    }

    /**
     * sort set 放入值
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public static Boolean zadd(String key, String value, Long score) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            jedis.zadd(REDIS_PREIFX + key, score, value);
            return true;
        } catch (Exception e) {
            logger.info("sort set 放入异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }
}
