package com.dlz.spring.scan.iproxy.anno;

import java.lang.annotation.*;

/**
 * 接口代理注解
 * @author dk
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnoApi {
	String value() default ""; 
	String handler() default ""; 
}
