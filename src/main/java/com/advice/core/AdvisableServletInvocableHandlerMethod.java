package com.advice.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import com.advice.api.ServiceAdvice;
import com.advice.api.ServiceAdviceChain;
import com.advice.api.ServiceAdviceChain.ChainIterator;
import com.advice.api.ServiceAdviceDispatchService;
import com.advice.domain.RequestParameter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;

import lombok.EqualsAndHashCode;

/**
 * 拓展{@link ServletInvocableHandlerMethod}
 *
 */
@EqualsAndHashCode(callSuper = true)
class AdvisableServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {

    private ServiceAdviceDispatchService dispatch;

    private static final Class<?>        OBJECT_ARRAY  = Object[].class;

    private static final Type            PARAMETER_MAP = TypeUtils.parameterize(Map.class, String.class, Object.class);

    public AdvisableServletInvocableHandlerMethod(HandlerMethod handlerMethod, ServiceAdviceDispatchService dispatch) {
        super(handlerMethod);
        this.dispatch = dispatch;
    }

    @Override
    protected Object doInvoke(Object... args) throws Exception {
        String path = getRequest().getServletPath();
        ServiceAdviceChain chain = dispatch.dispatch(this, path);
        int state = 0;
        Object response = null;

        if (Objects.nonNull(chain) && !chain.isEmpty()) {
            ChainIterator itr = chain.iterator();
            while (itr.hasNext()) { // before
                Object request = null;
                ServiceAdvice<Object, Object> advice = itr.next();
                HandlerMethod hm = this;
                for (Type type : advice.getClass().getGenericInterfaces()) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType parameterType = (ParameterizedType) type;

                        if (ServiceAdvice.class.equals(parameterType.getRawType())) {
                            Type actualType = parameterType.getActualTypeArguments()[0];
                            Class<?> requestType = TypeUtils.getRawType(actualType, null);

                            request = agentParameterProcess(path, hm, requestType, actualType, args);
                            break;
                        }
                    }

                }

                advice.beforeExcuteDoService(request);
                itr.setParameter(request);
            }

            response = super.doInvoke(args);
            itr.reset(); // reset
            state = ~state;

            while (itr.hasNext()) { // after
                ServiceAdvice<Object, Object> advice = itr.next();
                advice.afterDoService(itr.getParameter(), response);
            }

        }

        return state < 0 ? response : super.doInvoke(args);
    }

    // 解析ServiceAdvice需要的参数, 解析不了返回null
    private static final Object agentParameterProcess(String path, HandlerMethod hm, Class<?> requestType, Type actualType, Object... args) {
        if (Objects.isNull(requestType))
            return null;

        int count = 0;
        Object param = null;
        MethodParameter[] mps = hm.getMethodParameters();
        if (RequestParameter.class.isAssignableFrom(requestType)) { // RequestParameter
            HttpMethod httpMethod = HttpMethod.resolve(getRequest().getMethod());
            RequestParameter rp = (RequestParameter) BeanUtils.instantiate(requestType);
            List<PropertyDescriptor> pds = Lists.newArrayList(BeanUtils.getPropertyDescriptors(requestType));
            for (int i = 0; i < mps.length; i++) {
                MethodParameter mp = mps[i];
                Iterator<PropertyDescriptor> itr = pds.iterator();
                while (itr.hasNext()) {
                    PropertyDescriptor pd = itr.next();
                    if (mp.getParameterName().equals(pd.getName()) && // 属性名称相同且接收的类型可以接收实际类型
                        pd.getPropertyType().isAssignableFrom(mp.getParameterType())) {
                        try {
                            pd.getWriteMethod().invoke(rp, args[i]);
                            count++;
                        } catch (Exception e) {
                            throw new IllegalStateException(String.format("%s.%s 参数注入错误",
                                requestType.getSimpleName(), pd.getName()), e);
                        }
                        itr.remove();
                    }
                }
                rp.setRequestMapping(path);
                rp.setHttpMethod(httpMethod);
                param = rp;
            }

        } else if (OBJECT_ARRAY.equals(requestType)) { // Object[]
            param = Arrays.copyOf(args, args.length);
            count++;
        } else if (PARAMETER_MAP.equals(actualType)) { // Map<String, Object>
            if (args.length == 1 && actualType.equals(args[0].getClass())) {
                param = Objects.isNull(args[0]) ? Collections.<String, Object> emptyMap() : args[0];
            } else {
                Builder<String, Object> builder = ImmutableMap.builder();
                for (int i = 0; i < mps.length; i++) {
                    MethodParameter mp = mps[i];
                    builder.put(mp.getParameterName(), args[i]);
                }
                builder.put("httpMethod", HttpMethod.resolve(getRequest().getMethod()));
                builder.put("requestMapping", path);
                param = builder.build();
            }
            count++;
        } else { // Object
            for (Object arg : args) { // ...不用判null, 根据类型匹配参数
                if (requestType.isAssignableFrom(arg.getClass())) {
                    param = arg;
                    count++;
                    break;
                }
            }

        }

        Assert.isTrue(count < 1, String.format("无法解析ServiceAdvice所需参数 %s", requestType.getName()));

        return param;
    }

    private static final HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
}
