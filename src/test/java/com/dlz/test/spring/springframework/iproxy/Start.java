package com.dlz.test.spring.springframework.iproxy;

import com.dlz.test.spring.config.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class Start extends BaseTest {
//	@Before
//	public void setUp() throws Exception {
//		SpringHolder.init();
//		testService=(ITestApi)SpringHolder.getBean("iTestApi");
//		test2Service=(ITest2Api)SpringHolder.getBean("iTest2Api");
//	}

	@Autowired
	ITest2Api test2Service;

	@Autowired
	ITestApi testService;

    @Test
    public void test() {
        System.out.println(test2Service.sayHello("1","2"));
        System.out.println(testService.sayHello("-1","-2"));
        System.out.println(testService.sayHello("-1","-2"));
        System.out.println(test2Service.sayHello("-1"));
        System.out.println(test2Service.sayHello());
    }
}