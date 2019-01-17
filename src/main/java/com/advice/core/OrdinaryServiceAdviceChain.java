package com.advice.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import org.springframework.web.method.HandlerMethod;

import com.advice.api.ServiceAdvice;
import com.advice.api.ServiceAdviceChain;
import com.google.common.collect.Sets;

/**
 * 能保存上下文中参数的ServiceAdviceChain
 *
 * @see ServiceAdviceChain
 * @see LinkedList
 */
public class OrdinaryServiceAdviceChain implements ServiceAdviceChain {

    private String        path;

    private HandlerMethod method;

    private Set<Buket>    values = Sets.newLinkedHashSet();

    public OrdinaryServiceAdviceChain(String path, HandlerMethod method) {
        super();
        this.path = path;
        this.method = method;
    }

    public String getWildcard() {
        return path;
    }

    public HandlerMethod getMethod() {
        return method;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean add(ServiceAdvice<Object, Object> service) {
        return values.add(new Buket(service));
    }

    @Override
    public String servicePath() {
        return path;
    }

    @Override
    public HandlerMethod handlerMethod() {
        return method;
    }

    @Override
    public ChainIterator iterator() {
        return new ChainIteratorImpl(this);
    }

    /**
     * ChainIterator
     *
     * @see ServiceAdviceChain.ChainIterator
     */
    static class ChainIteratorImpl implements ServiceAdviceChain.ChainIterator {

        private Iterator<Buket>   delegate;

        private Buket             cur;

        private Collection<Buket> values;

        public ChainIteratorImpl(OrdinaryServiceAdviceChain chain) {
            values = chain.values;
            reset();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public ServiceAdvice<Object, Object> next() {
            cur = delegate.next();
            return cur.service;
        }

        public Object getParameter() {
            return Objects.isNull(cur) ? null : cur.parameter;
        }

        public void setParameter(Object parameter) {
            if (Objects.nonNull(cur))
                cur.parameter = parameter;
        }

        public void reset() {
            delegate = values.iterator();
        }

    }

    private class Buket {

        Buket(ServiceAdvice<Object, Object> service) {
            this.service = service;
        }

        ServiceAdvice<Object, Object> service;

        Object                        parameter;

    }

}
