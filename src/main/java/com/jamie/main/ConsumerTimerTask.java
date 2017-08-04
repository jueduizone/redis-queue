package com.jamie.main;

import com.jamie.model.Job;
import com.jamie.service.RedisQueueConsumerService;
import com.jamie.service.impl.RedisQueueConsumerServiceImpl;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConsumerTimerTask{
    /**
     * 线程池的管理工具
     * 调度型线程池
     */
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Job.JobTopic.values().length * 2);

    public static void execute(){
        for (final Job.JobTopic topic : Job.JobTopic.values()){
            scheduler.scheduleAtFixedRate(new Runnable() {
                RedisQueueConsumerService redisQueueConsumerService = new RedisQueueConsumerServiceImpl();
                public void run() {
                    Thread.currentThread().setName("DealBucket_" + topic.name());
                    redisQueueConsumerService.dealBucket(topic);
                }
            },1,100, TimeUnit.MILLISECONDS);

            scheduler.scheduleAtFixedRate(new Runnable() {
                RedisQueueConsumerService redisQueueConsumerService = new RedisQueueConsumerServiceImpl();
                public void run() {
                    Thread.currentThread().setName("DealReady_" + topic.name());
                    try {
                        redisQueueConsumerService.dealReadyQueue(topic);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },0,100, TimeUnit.MILLISECONDS);
        }
    }


}
