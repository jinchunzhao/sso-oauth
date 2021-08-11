
package com.jy.sso.config.request;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 重写HttpServletRequest
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 11:35
 */
public final class SsoHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders;

    public SsoHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.customHeaders = new ConcurrentHashMap<>();

    }

    /**
     * 往header中添加参数
     *
     * @param key 
     *            键
     * @param value
     *            值
     */
    public void putHeader(String key, String value) {
        this.customHeaders.put(key, value);
    }

    @Override
    public String getHeader(String key) {
        String headerValue = customHeaders.get(key);
        if (Objects.nonNull(headerValue)) {
            return headerValue;
        }
        return ((HttpServletRequest) getRequest()).getHeader(key);

    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(customHeaders.keySet());
        @SuppressWarnings("unchecked")
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            String n = e.nextElement();
            set.add(n);
        }
        return Collections.enumeration(set);
    }
}