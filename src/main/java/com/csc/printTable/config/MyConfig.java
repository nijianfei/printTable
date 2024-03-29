package com.csc.printTable.config;

import com.alibaba.fastjson.JSONObject;
import com.csc.printTable.dto.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;



@Configuration
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class MyConfig {
    @Value("${print.template}")
    private String templateStr;

    @Bean
    public Template templateConfig() {
        return JSONObject.parseObject(templateStr,Template.class);
    }
}
