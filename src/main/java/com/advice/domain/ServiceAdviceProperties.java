package com.advice.domain;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import com.advice.core.DefaultServiceAdviceDispatchService;
import com.google.common.collect.Lists;

import lombok.Data;

/**
 * ServiceAdviceProperties
 *
 * @see DefaultServiceAdviceDispatchService
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "service-advice")
public class ServiceAdviceProperties {

    private Map<String, AdviceModel> advices;

    private List<String>             disable = Lists.newLinkedList();

    /**
     * AdviceModel
     *
     */
    @Data
    public static class AdviceModel {

        private String       url;

        private List<String> services;

    }
}
