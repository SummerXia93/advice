package com.advice.core;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.advice.api.ServiceAdviceDispatchService;
import com.advice.domain.ServiceAdviceProperties;

/**
 * ServiceAdviceConfiguration
 *
 */
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 15)
@EnableConfigurationProperties({ ServiceAdviceProperties.class })
public class ServiceAdviceConfiguration {

    @Bean
    public DefaultServiceAdviceDispatchService defaultServiceAdviceDispatchService() {
        return new DefaultServiceAdviceDispatchService();
    }

    @Bean
    @Primary
    public RequestMappingHandlerAdapterExpand requestMappingHandlerAdapterExpand(
        RequestMappingHandlerAdapter adapter,ServiceAdviceDispatchService service) {
        return new RequestMappingHandlerAdapterExpand(adapter, service);
    }

}
