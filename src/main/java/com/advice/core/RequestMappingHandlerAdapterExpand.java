package com.advice.core;

import org.springframework.beans.BeanUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import com.advice.api.ServiceAdviceDispatchService;

/**
 * RequestMappingHandlerAdapterExpand
 *
 * @see RequestMappingHandlerAdapter
 */
class RequestMappingHandlerAdapterExpand extends RequestMappingHandlerAdapter {

    private ServiceAdviceDispatchService service;

    RequestMappingHandlerAdapterExpand(RequestMappingHandlerAdapter adapter, ServiceAdviceDispatchService service) {
        BeanUtils.copyProperties(adapter, this, RequestMappingHandlerAdapter.class);
        this.service = service;
    }

    @Override
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new AdvisableServletInvocableHandlerMethod(handlerMethod, service);
    }

}