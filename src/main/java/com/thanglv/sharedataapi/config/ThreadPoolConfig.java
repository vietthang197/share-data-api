package com.thanglv.sharedataapi.config;

import org.apache.tomcat.util.threads.VirtualThreadExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public VirtualThreadExecutor virtualThreadExecutor() {
        return new VirtualThreadExecutor("VT-executor-sharedataapi");
    }
}
