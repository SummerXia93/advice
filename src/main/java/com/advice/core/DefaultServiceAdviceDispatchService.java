package com.advice.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;

import com.advice.api.ServiceAdvice;
import com.advice.api.ServiceAdviceDispatchService;
import com.advice.domain.ServiceAdviceProperties;
import com.advice.domain.ServiceAdviceProperties.AdviceModel;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

/**
 * DefaultServiceAdviceDispatchService
 *
 * @see ServiceAdviceDispatchService
 */
@SuppressWarnings("rawtypes")
public class DefaultServiceAdviceDispatchService implements ServiceAdviceDispatchService, ApplicationContextAware, InitializingBean {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultServiceAdviceDispatchService.class);

    private ApplicationContext         context;

    private Map<String, ServiceAdvice> services;

    private ServiceAdviceProperties    properties;

    @Override
    @SuppressWarnings("unchecked")
    public OrdinaryServiceAdviceChain dispatch(HandlerMethod method, String path) {
        OrdinaryServiceAdviceChain chain = new OrdinaryServiceAdviceChain(path, method);
        List<String> disable = properties.getDisable();
        if (MapUtils.isNotEmpty(properties.getAdvices())) {

            Iterator<Entry<String, AdviceModel>> itr = properties.getAdvices().entrySet().iterator();

            while (itr.hasNext()) {
                Entry<String, AdviceModel> entry = itr.next();
                if (disable.contains(entry.getKey()))
                    continue;

                AdviceModel adviceModel = entry.getValue();

                if (StringUtils.equals(path, adviceModel.getUrl())
                    && CollectionUtils.isNotEmpty(adviceModel.getServices())) {
                    for (String name : adviceModel.getServices()) {
                        ServiceAdvice advice = services.get(name);
                        Assert.notNull(advice,
                            String.format("请求路径 [%s] 对应配置的ServiceAdvice中, 名称为 %s 的Bean没有找到",
                                path, name));
                        chain.add(advice);
                    }
                }
            }

        }
        return chain;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        Iterator<Entry<String, ServiceAdvice>> itr = context.getBeansOfType(ServiceAdvice.class).entrySet().iterator();
        StringBuilder message = new StringBuilder("DefaultServiceAdviceDispatchService registered --> [");
        ImmutableMap.Builder builder = ImmutableMap.builder();
        while (itr.hasNext()) {
            Entry<String, ServiceAdvice> e = itr.next();
            builder.put(e.getKey(), e.getValue());
            message.append(e.getKey());
            message.append(' ');
            message.append(',');
        }
        if (',' == message.charAt(message.length() - 1))
            message.delete(message.length() - 2, message.length());
        message.append(']');
        log.info(message.toString());

        DefaultServiceAdviceDispatchService.this.services = builder.build();
        DefaultServiceAdviceDispatchService.this.properties = context.getBean(ServiceAdviceProperties.class);

        log.info("DefaultServiceAdviceDispatchService Advices Properties --> %s", JSON.toJSONString(properties.getAdvices()));
    }

}
