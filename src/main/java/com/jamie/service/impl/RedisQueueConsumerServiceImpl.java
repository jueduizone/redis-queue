package com.jamie.service.impl;

import com.jamie.model.Job;
import com.jamie.service.RedisQueueConsumerService;
import com.jamie.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public class RedisQueueConsumerServiceImpl implements RedisQueueConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(RedisQueueConsumerServiceImpl.class);

    /**
     * 处理对应的Bucket
     *
     * @param topic
     * @return
     */
    public Boolean dealBucket(Job.JobTopic topic) {
//        logger.info("{} : {} running", Thread.currentThread().getName(), Thread.currentThread().getId());
        Long now = System.currentTimeMillis() / 1000;
        //Redis 加锁
        Boolean lock = RedisUtil.setnx(RedisUtil.REDIS_JOB_BUCKET_LOCK + topic.name(), String.valueOf(now), 300);
        if (!lock) {
            logger.info("{}redis block", topic.name());
            return false;
        }
        Set<String> result = RedisUtil.ZRangeByScore(RedisUtil.REDIS_JOB_BUCKET + topic, "-inf", String.valueOf(now));
        if (result == null || result.size() == 0) {
            //Redis 解锁
            RedisUtil.del(RedisUtil.REDIS_JOB_BUCKET_LOCK + topic.name());
            return false;
        }
        Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            String jobId = iterator.next();
            logger.info("Bucket Queue找到topic为：{} 的 JobId:{}", topic.name(), jobId);
            Job job = (Job) RedisUtil.getObject(jobId);
            if (job != null && now >= job.getDelay()) {
                logger.info("Bucket Queue处理topic为：{} 的 Job:{}", topic.name(), job.toString());
                //放入ready queue
                Boolean push = RedisUtil.lpush(RedisUtil.REDIS_JOB_READY + topic.name(), jobId);
                if (push) {
                    RedisUtil.ZRem(RedisUtil.REDIS_JOB_BUCKET + topic.name(), jobId);
                }
            }
        }
        //Redis 解锁
        RedisUtil.del(RedisUtil.REDIS_JOB_BUCKET_LOCK + topic.name());
        return true;
    }

    /**
     * 处理进行队列
     *
     * @param topic
     * @return
     */
    public Boolean dealReadyQueue(Job.JobTopic topic) throws InterruptedException {
//        logger.info("{} : {} running", Thread.currentThread().getName(), Thread.currentThread().getId());
        String jobId = RedisUtil.brpop(RedisUtil.REDIS_JOB_READY + topic.name(), 0);
        Job job = (Job) RedisUtil.getObject(jobId);
        if (job == null) {
            return false;
        }
        Long currentTime = System.currentTimeMillis() / 1000;
        logger.info("Ready Queue开始处理Topic为{}的Job，内容为{}的业务逻辑，延迟2s", topic.name(), job.toString());
        Thread.sleep(2000);
        logger.info("Ready Queue处理完任务 {} 的时间: {},相差:{} 秒",job.getId(), currentTime, (currentTime - job.getDelay()));
        //从pool中删除资源
        RedisUtil.delObject(jobId);
        return true;
    }
}
