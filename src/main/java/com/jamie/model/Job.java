package com.jamie.model;

import java.io.Serializable;

/**
 * 任务元信息，需要序列化
 */
public class Job implements Serializable {
    //任务id
    private String id;

    public enum JobTopic {
        //订单待审核
        order_verfiy,
        //订单待支付
        order_pay,
        //订单待发货
        order_shipping
    }

    //任务主题
    private JobTopic jobTopic;

    //任务延迟多少时间执行
    private Long delay;
    //任务执行超时时间
    private Integer timeout;
    //Job的内容，供消费者做具体的业务处理，以json格式存储
    private String body;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JobTopic getJobTopic() {
        return jobTopic;
    }

    public void setJobTopic(JobTopic jobTopic) {
        this.jobTopic = jobTopic;
    }


    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", jobTopic=" + jobTopic +
                ", delay=" + delay +
                ", timeout=" + timeout +
                ", body='" + body + '\'' +
                '}';
    }
}
