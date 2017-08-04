package com.jamie.service;

import com.jamie.model.Job;

public interface RedisQueueProducerService {

    /**
     * job放入pool和bucket
     *
     * @param job
     * @return
     */
    Boolean putJobToPool(Job job);
}
