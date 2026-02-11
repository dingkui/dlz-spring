package com.dlz.test.spring.redis.queue;

import com.dlz.spring.redis.queue.annotation.AnnoRedisQueueProvider;
import com.dlz.spring.scan.iproxy.anno.AnnoApi;

import java.util.List;

//通过RedisQueue接口定义消息队列生产者interface
//通过RedisQueueProvider接口定义生产者队列名称
@AnnoApi(handler = "redisQueueProvider")
public interface ITestQueueProviderApi {
    @AnnoRedisQueueProvider(value = "queue1")
    void sendMessage1(String msg);

    @AnnoRedisQueueProvider(value = "queue2")
    void sendMessage2(List<String> msgList);

    @AnnoRedisQueueProvider(value = "queue3")
    void sendMessage3(String test);
}