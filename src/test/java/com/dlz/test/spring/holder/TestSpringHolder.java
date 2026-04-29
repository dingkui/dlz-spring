package com.dlz.test.spring.holder;

import com.dlz.kit.util.id.TraceUtil;
import com.dlz.spring.holder.SpringHolder;
import com.dlz.test.spring.config.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
@Slf4j
public class TestSpringHolder extends BaseTest {
	@Test
	public void t1(){
		TraceUtil.setTraceId("t1");
		BeanClass2 beanClass2 = SpringHolder.registerBean(BeanClass2.class, false);
		BeanClass2 beanClass21 = SpringHolder.registerBean(BeanClass2.class, false);
		BeanClass2 beanClass23 = SpringHolder.registerBean(BeanClass2.class, false);
		log.debug(beanClass2+" " +beanClass2.getBeanClass1());
		log.debug(beanClass21+" " +beanClass21.getBeanClass1());
		log.debug(beanClass23+" " +beanClass23.getBeanClass1());
		log.trace("111");
		log.info("111");
		log.warn("111");
		log.error("111");
	}
}
