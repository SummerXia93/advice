package com.advice.domain;

import org.springframework.http.HttpMethod;

import com.advice.api.ServiceAdvice;

import lombok.Data;

/**
 * {@link ServiceAdvice}使用得请求参数, 继承此类添加属性即可
 *
 */
@Data
public class RequestParameter {

    /**
     * 请求路径
     */
    private String     requestMapping;

    /**
     * {@link HttpMethod}
     */
    private HttpMethod httpMethod;

}
