package com.dlz.test.spring;

import com.dlz.test.spring.config.DlzFwConfigs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.atomic.AtomicInteger;

@ConfigurationPropertiesScan
@SpringBootApplication()
@Import({DlzFwConfigs.class})
public class Starter implements ApplicationListener<ContextRefreshedEvent> {
    private static final AtomicInteger CONTEXT_REFRESH_COUNT = new AtomicInteger(0);

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        int count = CONTEXT_REFRESH_COUNT.incrementAndGet();
        System.out.println("Spring 容器启动完成，第 " + count + " 次");
    }

    public static int getContextRefreshCount() {
        return CONTEXT_REFRESH_COUNT.get();
    }
}