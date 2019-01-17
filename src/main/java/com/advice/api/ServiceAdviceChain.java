package com.advice.api;

import java.util.Iterator;
import java.util.LinkedList;

import org.springframework.web.method.HandlerMethod;

/**
 * ServiceAdviceChain
 *
 * @see LinkedList
 */
public interface ServiceAdviceChain extends Iterable<ServiceAdvice<Object, Object>> {

    boolean isEmpty();

    ChainIterator iterator();

    String servicePath();
    
    HandlerMethod handlerMethod();

    /**
     * Iterator
     *
     */
    public static interface ChainIterator extends Iterator<ServiceAdvice<Object, Object>> {

        public Object getParameter();

        public void setParameter(Object parameter);

        public void reset();

    }

}
