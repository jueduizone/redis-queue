package com.jamie.service;

import com.jamie.model.Job;

public interface RedisQueueProducerService {
    Boolean putJobToPool(Job job);
}
