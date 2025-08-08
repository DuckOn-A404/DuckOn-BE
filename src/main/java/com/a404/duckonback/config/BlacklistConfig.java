package com.a404.duckonback.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BlacklistProperties.class)
public class BlacklistConfig {}
