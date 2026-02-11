//package com.dlz.test.spring.redis.queue;
//
//import com.dlz.spring.redis.queue.annotation.AnnoRedisQueueConsumer;
//import com.dlz.spring.redis.queue.consumer.ARedisQueueConsumer;
//import org.springframework.stereotype.Component;
//
//@Component
//@AnnoRedisQueueConsumer("queue1")
//public class MessageConsumer1 extends ARedisQueueConsumer<String> {
//	@Override
//	public void doConsume(String message) {
//		System.out.println(message);
//	}
//}