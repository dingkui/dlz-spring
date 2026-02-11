package com.dlz.test.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

//@SpringBootApplication
//@EnableAsync
//@ConfigurationPropertiesScan
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"com.dlz.spring", "com.dlz.test.spring.config"})
public class Starter {

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
//        System.out.println((String)HttpEnum.POST.send("http://dk.d.shunliannet.com"));
    }
}