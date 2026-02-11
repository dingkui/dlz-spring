package com.dlz.test.spring.springframework.iproxy;

import com.dlz.spring.scan.iproxy.anno.AnnoApi;

@AnnoApi(handler = "test")
public interface ITest2Api{
    String sayHello(String a,String b);
    String sayHello(String a);
    String sayHello();
}