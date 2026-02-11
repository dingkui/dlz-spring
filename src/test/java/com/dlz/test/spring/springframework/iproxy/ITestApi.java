package com.dlz.test.spring.springframework.iproxy;

import com.dlz.spring.scan.iproxy.anno.AnnoApi;

@AnnoApi(handler = "test")
public interface ITestApi{
    String sayHello(String a,String b);
}