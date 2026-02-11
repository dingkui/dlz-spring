package com.dlz.spring.config;

import com.dlz.comm.cache.CacheUtil;
import com.dlz.comm.cache.ICache;
import com.dlz.comm.util.StringUtils;
import com.dlz.spring.cache.aspect.CacheAspect;
import com.dlz.spring.holder.SpringHolder;
import com.dlz.spring.redis.excutor.JedisExecutor;
import com.dlz.spring.redis.queue.provider.RedisQueueProviderApiHandler;
import com.dlz.spring.redis.util.IKeyMaker;
import com.dlz.spring.redis.util.RedisKeyMaker;
import com.dlz.spring.scan.iproxy.ApiProxyHandler;
import com.dlz.spring.scan.iproxy.ApiScaner;
import com.dlz.spring.scan.scaner.DlzSpringScaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisPool;

/**
 * @author dk
 * date 2020-10-15
 */
@Slf4j
@EnableConfigurationProperties({DlzProperties.class})
public class DlzFwConfig {
    /**
     * spring 容器启动开始执行
     */
    @Bean
    public BeanFactoryPostProcessor myBeanFactory(Environment env) {
        return beanFactory -> {
            SpringHolder.init(beanFactory);
            String apiScanPath = env.getProperty("dlz.fw.api-scan-path");
            if (StringUtils.isNotEmpty(apiScanPath)) {
                if(log.isInfoEnabled()) {
                    log.info("dlz spring apiScan init,resoucePath:{}", apiScanPath);
                }
                new DlzSpringScaner().doComponents(new ApiScaner(apiScanPath));
            }
        };
    }

    /**
     * 缓存实现
     */
    @Bean(name = "dlzCache")
    @ConditionalOnMissingBean(name = "dlzCache")
    @Lazy
    public ICache dlzCache(DlzProperties properties) throws InstantiationException, IllegalAccessException {
        Class<? extends ICache> cacheClass = properties.getCache().getCacheClass();
        if(log.isInfoEnabled()){
            log.info("init dlzCache:" + cacheClass.getName());
        }
        ICache iCache = cacheClass.newInstance();
        CacheUtil.init(iCache);
        return iCache;
    }

    /**
     * redis key构建器
     */
    @Bean(name = "redisKeyMaker")
    @ConditionalOnMissingBean(name = "redisKeyMaker")
    @Lazy
    public IKeyMaker redisKeyMaker() {
        if(log.isInfoEnabled()){
            log.info("init redisKeyMaker:"+RedisKeyMaker.class.getName());
        }
        return new RedisKeyMaker();
    }

    @Bean(name = "redisPool")
    @ConditionalOnMissingBean(name = "redisPool")
    @Lazy
    public JedisPool redisPool() {
        JedisConfig jedisConfig = SpringHolder.registerBean(JedisConfig.class);
        final JedisPool jedisPool = jedisConfig.redisPoolFactory();
        if(log.isInfoEnabled()){
            log.info("init default redisPool:"+jedisPool.getClass().getName());
        }
        return jedisPool;
    }

    /**
     * 缓存切面
     * dlz.cache.anno=true时生效
     * @param cache
     */
    @Bean
    @ConditionalOnProperty(value = "dlz.cache.anno", havingValue = "true")
    public CacheAspect cacheAspect(ICache cache) {
        if(log.isInfoEnabled()){
            log.info("init cacheAspect：dlz.cache.anno=true");
        }
        return new CacheAspect(cache);
    }

    /**
     * redis生产者消费者模式,开启本功能依赖开启dlz.fw.api-scan-path路径扫描
     */
    @Bean(name = "redisQueueProviderApiHandler")
    @Lazy
    @ConditionalOnMissingBean(name = "redisQueueProviderApiHandler")
    public ApiProxyHandler redisQueueProviderApiHandler() {
        if(log.isInfoEnabled()){
            log.info("init redisQueueProviderApiHandler:"+RedisQueueProviderApiHandler.class.getName());
        }
        return new RedisQueueProviderApiHandler();
    }

    @Bean(name = "jedisExecutor")
    @Lazy
    @ConditionalOnMissingBean(name = "jedisExecutor")
    public JedisExecutor jedisExecutor(JedisPool jedisPool,IKeyMaker keyMaker) {
        if(log.isInfoEnabled()){
            log.info("init jedisExecutor:"+JedisExecutor.class.getName());
        }
        return new JedisExecutor(jedisPool,keyMaker);
    }

    /**
     * 系统配置取值器
     */
    @Bean
    @Lazy
    public BootConfig bootConfig() {
        if(log.isInfoEnabled()){
            log.info("init BootConfig:"+BootConfig.class.getName());
        }
        return new BootConfig();
    }

    @Bean(name = "customConfig")
    @Lazy
    @ConditionalOnMissingBean(name = "customConfig")
    public ICustomConfig customConfig() {
        if(log.isInfoEnabled()){
            log.info("init customConfig  :"+ICustomConfig.class.getName());
        }
        return new ICustomConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public void set(String key, String value) {
            }
        };
    }
}
