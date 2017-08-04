package com.jamie.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.List;
import java.util.Set;

public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static final String REDIS_IP = "127.0.0.1";
    private static final Integer REDIS_PORT = 6379;
    private static final String REDIS_PREIFX = "Delay_Queue_";
    public static final String REDIS_JOB_POOL = "Job_Pool";
    public static final String REDIS_JOB_READY = "Ready_Queue_";
    public static final String REDIS_JOB_BUCKET = "Bucket_Queue_";
    public static final String REDIS_JOB_BUCKET_LOCK = "Bucket_Lock_";
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
            logger.info("delObject 删除 key 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static void del(String key) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            jedis.del(REDIS_PREIFX + key);
        } catch (Exception e) {
            logger.info("del 删除key异常");
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
            if (byteVal != null) {
                o = KryoUtil.deserialize(byteVal);
            }
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
    public static String brpop(String key, int timeout) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            //取出列表的最后一个元素，先进先出,阻塞
            List<String> list = jedis.brpop(timeout, REDIS_PREIFX + key);
            if (list != null && list.size() > 0) {
                return list.get(1);
            }
        } catch (Exception e) {
            logger.info("redis rpop 取出 list 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * sort set 放入值
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public static Boolean ZAdd(String key, String value, Long score) {
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


    /**
     * 取出sort set中指定区间score的值
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public static Set<String> ZRangeByScore(String key, String min, String max) {
        Jedis jedis = RedisUtil.getJedis();
        Set<String> result = null;
        try {
            result = jedis.zrangeByScore(REDIS_PREIFX + key, min, max);
        } catch (Exception e) {
            logger.info("sort set ZRangeByScore 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    /**
     * sort set 移除元素
     *
     * @param key
     * @param member
     * @return
     */
    public static Boolean ZRem(String key, String member) {
        Jedis jedis = RedisUtil.getJedis();
        try {
            jedis.zrem(REDIS_PREIFX + key, member);
            return true;
        } catch (Exception e) {
            logger.info("sort set Zrem 异常");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * setnx锁操作
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    public static Boolean setnx(String key, String value, int expire) {
        Jedis jedis = getJedis();
        try {
            Long result = jedis.setnx(REDIS_PREIFX + key, value);
            if (result == 1) {
                //设置成功，加过期时间
                jedis.expire(REDIS_PREIFX + key, expire);
                return true;
            }
        } catch (Exception e) {
            logger.error("setnx锁操作!", e);
        } finally {
            if (null != jedis)
                jedis.close();
        }
        return false;
    }
}
