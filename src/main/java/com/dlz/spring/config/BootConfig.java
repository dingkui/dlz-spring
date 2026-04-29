package com.dlz.spring.config;


import com.dlz.kit.json.IUniversalVals;
import com.dlz.kit.json.JSONMap;
import com.dlz.kit.util.StringUtils;
import com.dlz.kit.util.config.ConfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.function.Function;

/**
 * 系统配置取值器
 * 取值优先级：
 * springboot配置 > config.txt > 客户端自定义配置
 */
public class BootConfig implements IUniversalVals {
    /**
     * spring boot配置环境变量
     */
    @Autowired
    Environment environment;
    /**
     * spring boot 和 config 配置本地缓存
     */
    private final JSONMap map = new JSONMap();
    /**
     * 客户端自定义取得配置的服务
     */
    @Autowired
    ICustomConfig customConfig;

    /**
     * 环境，方便在代码中获取
     *
     * @return 环境 env
     */
    public String getEnv() {
        Objects.requireNonNull(environment, "Spring boot 环境下 Environment 不可能为null");
        String env = environment.getProperty("spring.profiles.active");
        Assert.notNull(env, "请使用 DlzApplication 启动...");
        return env;
    }

    /**
     * 应用名称${spring.application.name}
     *
     * @return 应用名
     */
    public String getName() {
        Objects.requireNonNull(environment, "Spring boot 环境下 Environment 不可能为null");
        return environment.getProperty("spring.application.name", "dlz");
    }

    @Override
    public Object getInfoObject() {
        return map;
    }

    private Function<String, Object> getStrFn = (name) -> {
        Object val = map.getKeyVal(name);
        if (val != null) {
            return val;
        }
        val = environment.getProperty(name);
        if (val == null) {
            val = ConfUtil.getConfig(name);
        }
        if (val == null) {
            return customConfig.get(name);
        }
        return val;
    };

    @Override
    public Object getKeyVal(String key) {
        return StringUtils.getReplaceStr(key, getStrFn, 0);
    }

}
