package com.mojagap.backenstarter.infrastructure.logger;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.utility.CommonUtil;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.user.PlatformTypeEnum;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.repository.user.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class UserActivityLogFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(UserActivityLogFilter.class.getName());
    private final UserActivityLogRepository userActivityLogRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    public UserActivityLogFilter(UserActivityLogRepository userActivityLogRepository, HandlerExceptionResolver handlerExceptionResolver) {
        this.userActivityLogRepository = userActivityLogRepository;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        try {
            long startTime = System.currentTimeMillis();
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) servletResponse);
            if (!requestWrapper.getMethod().equals(HttpMethod.GET.name())) {
                Integer platformType = Integer.valueOf(requestWrapper.getHeader(ApplicationConstants.PLATFORM_TYPE_HEADER_KEY));
                PlatformTypeEnum platformTypeEnum = PlatformTypeEnum.fromInt(platformType);
                UserActivityLog userActivityLog = initializeUserActivityLog(requestWrapper);
                userActivityLog.setPlatformType(platformTypeEnum.getId());
                requestWrapper.setAttribute(UserActivityLog.class.getName(), userActivityLog);
            }

            filterChain.doFilter(requestWrapper, responseWrapper);

            UserActivityLog userActivityLog = (UserActivityLog) requestWrapper.getAttribute(UserActivityLog.class.getName());
            if (userActivityLog != null) {
                String requestBody = new String(requestWrapper.getContentAsByteArray());
                String responseBody = new String(responseWrapper.getContentAsByteArray());
                userActivityLog.setRequestBody(requestBody);
                userActivityLog.setResponseBody(responseBody);
                responseWrapper.copyBodyToResponse();
                setHttpResponseProperties(responseWrapper, userActivityLog);
                long endTime = System.currentTimeMillis();
                userActivityLog.setDuration((int) (endTime - startTime));
                userActivityLogRepository.saveAndFlush(userActivityLog);
            } else {
                responseWrapper.copyBodyToResponse();
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Failure Logging User Activities : ", ex);
            handlerExceptionResolver.resolveException((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, null, ex);
        }
    }

    private UserActivityLog initializeUserActivityLog(ContentCachingRequestWrapper requestWrapper) throws IOException {
        UserActivityLog userActivityLog = new UserActivityLog();
        setUserActivityLogProps(userActivityLog, requestWrapper.getRequestURI(), requestWrapper.getQueryString(), requestWrapper.getMethod(),
                BaseController.getRequestHeaders(requestWrapper), requestWrapper.getRemoteAddr());
        return userActivityLog;
    }

    public static void setUserActivityLogProps(UserActivityLog userActivityLog, String requestURI, String queryString, String method, String requestHeaders, String remoteAddress) {
        String fullUrl = queryString != null ? requestURI + "?" + queryString : requestURI;
        userActivityLog.setRequestUrl(fullUrl);
        userActivityLog.setRequestMethod(method);
        userActivityLog.setRequestHeaders(requestHeaders);
        userActivityLog.setRemoteIpAddress(remoteAddress);
        userActivityLog.setCreatedOn(DateUtil.now());
    }

    private void setHttpResponseProperties(ContentCachingResponseWrapper responseWrapper, UserActivityLog userActivityLog) throws JsonProcessingException {
        String responseHeaders = getResponseHeaders(responseWrapper);
        userActivityLog.setResponseHeaders(responseHeaders);
        userActivityLog.setResponseStatusCode(responseWrapper.getStatus());
    }

    private String getResponseHeaders(ContentCachingResponseWrapper responseWrapper) throws JsonProcessingException {
        Map<String, String> headerMap = new HashMap<>();
        Collection<String> headerNames = responseWrapper.getHeaderNames();
        for (String key : headerNames) {
            String value = responseWrapper.getHeader(key);
            headerMap.put(key, value);
        }
        return CommonUtil.OBJECT_MAPPER.writeValueAsString(headerMap);
    }
}
