package com.a404.duckonback.common.config;

import com.a404.duckonback.common.interceptor.ApiBrowserRedirectInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final ApiBrowserRedirectInterceptor interceptor;

    public WebMvcConfig(ApiBrowserRedirectInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
