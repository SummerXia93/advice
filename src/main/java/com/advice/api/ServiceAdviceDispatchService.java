package com.advice.api;

import org.springframework.web.method.HandlerMethod;

/**
 * 指派{@link ServiceAdvice}
 *
 */
public interface ServiceAdviceDispatchService {

    ServiceAdviceChain dispatch(HandlerMethod method, String path);

}
