package com.jamie.service;

import com.jamie.model.Job;

/**
 * redis 队列消费者
 * jamie
 */
public interface RedisQueueConsumerService {
    /**
     * 处理对应的Bucket
     *
     * @param topic
     * @return
     */
    Boolean dealBucket(Job.JobTopic topic);

    /**
     * 处理进行队列
     *
     * @param topic
     * @return
     */
    Boolean dealReadyQueue(Job.JobTopic topic) throws InterruptedException;
}
