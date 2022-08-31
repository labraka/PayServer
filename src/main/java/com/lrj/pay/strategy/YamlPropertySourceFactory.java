package com.lrj.pay.strategy;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @ClassName: YamlPropertySourceFactory
 * @Description: yaml转properties加载配置转换工厂
 * @Date: 2022/8/10 14:28
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public class YamlPropertySourceFactory extends DefaultPropertySourceFactory {
    public YamlPropertySourceFactory() {
        super();
    }

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = Optional.ofNullable(name).orElse(resource.getResource().getFilename());
        if (!resource.getResource().exists()) {
            // return an empty Properties
            return new PropertiesPropertySource(sourceName, new Properties());
        } else if (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml")) {
            Properties propertiesFromYaml = loadYaml(resource);
            return new PropertiesPropertySource(sourceName, propertiesFromYaml);
        } else {
            return super.createPropertySource(name, resource);
        }
    }

    /**
     * 加载YAML文件
     * @param resource YAML文件
     * @return Properties对象
     * @throws IOException 文件加载异常
     */
    private Properties loadYaml(EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
