package com.advice.api;

import com.advice.domain.RequestParameter;

/**
 * 执行先后处理
 *
 * @param <P> 请求参数 {@link RequestParameter} 或 自定义对象接收(自定义对象为对应HandlerMethod参数中的对象)
 * @param <R> 响应参数
 */
public interface ServiceAdvice<P, R> {

    /**
     * 执行{@code HandlerMethod} 之前运行
     *
     * @param request
     */
    void beforeExcuteDoService(P request);

    /**
     * 执行{@code HandlerMethod} 之后
     *
     * @param request
     * @param response
     */
    void afterDoService(P request, R response);

}
