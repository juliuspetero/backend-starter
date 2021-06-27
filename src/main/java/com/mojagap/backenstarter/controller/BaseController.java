package com.mojagap.backenstarter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.logger.UserActivityLogFilter;
import com.mojagap.backenstarter.infrastructure.utility.CommonUtil;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.http.HttpResponseStatusEnum;
import com.mojagap.backenstarter.model.user.PlatformTypeEnum;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseController {

    private static final Logger LOG = Logger.getLogger(BaseController.class.getName());

    @Autowired
    protected HttpServletRequest httpServletRequest;

    protected UserActivityLog getUserActivityLog() {
        return (UserActivityLog) httpServletRequest.getAttribute(UserActivityLog.class.getName());
    }

    private void setUserActivityLogProperties(EntityTypeEnum entityTypeEnum, ActionTypeEnum actionTypeEnum, HttpResponseStatusEnum httpResponseStatusEnum, Throwable ex) {
        UserActivityLog userActivityLog = setUserActivityLogProperties(entityTypeEnum, actionTypeEnum, httpResponseStatusEnum);
        if (ex != null && userActivityLog != null) {
            String stackTrace = ExceptionUtils.getStackTrace(ex);
            userActivityLog.setStackTrace(stackTrace);
        }
    }

    private UserActivityLog setUserActivityLogProperties(EntityTypeEnum entityTypeEnum, ActionTypeEnum actionTypeEnum, HttpResponseStatusEnum httpResponseStatusEnum) {
        UserActivityLog userActivityLog = getUserActivityLog();
        if (userActivityLog != null) {
            userActivityLog.setEntityType(entityTypeEnum);
            userActivityLog.setActionType(actionTypeEnum);
            userActivityLog.setResponseStatus(httpResponseStatusEnum);
            if (AppContext.getLoggedInUser() != null) {
                userActivityLog.setUserId(AppContext.getLoggedInUser().getId());
            }
        }
        return userActivityLog;
    }

    protected <R> R executeAndLogUserActivity(EntityTypeEnum entityTypeEnum, ActionTypeEnum actionTypeEnum, Function<UserActivityLog, R> function) {
        UserActivityLog userActivityLog = setUserActivityLogProperties(entityTypeEnum, actionTypeEnum, HttpResponseStatusEnum.PENDING);
        try {
            R response = function.apply(userActivityLog);
            setUserActivityLogProperties(entityTypeEnum, actionTypeEnum, HttpResponseStatusEnum.SUCCESS, null);
            return response;
        } catch (Exception ex) {
            setUserActivityLogProperties(entityTypeEnum, actionTypeEnum, HttpResponseStatusEnum.FAILED, ex);
            LOG.log(Level.SEVERE, ex.getMessage() + " : " + getUserActivityLog(), ex);
            throw ex;
        }
    }

    @SneakyThrows
    protected <R> R executeHttpGet(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception ex) {
            logHttpGetUserActivity(ex);
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
    }

    @SneakyThrows(JsonProcessingException.class)
    private void logHttpGetUserActivity(Throwable ex) {
        UserActivityLog userActivityLog = getUserActivityLog();
        if (userActivityLog == null) {
            userActivityLog = new UserActivityLog();
        }
        if (httpServletRequest.getHeader(ApplicationConstants.PLATFORM_TYPE_HEADER_KEY) != null) {
            Integer platformType = Integer.valueOf(httpServletRequest.getHeader(ApplicationConstants.PLATFORM_TYPE_HEADER_KEY));
            PlatformTypeEnum platformTypeEnum = PlatformTypeEnum.fromInt(platformType);
            userActivityLog.setPlatformType(platformTypeEnum.getId());
        }
        UserActivityLogFilter.setUserActivityLogProps(userActivityLog, httpServletRequest.getRequestURI(), httpServletRequest.getQueryString(),
                httpServletRequest.getMethod(), getRequestHeaders(httpServletRequest), httpServletRequest.getRemoteAddr());
        String stackTrace = ExceptionUtils.getStackTrace(ex);
        userActivityLog.setStackTrace(stackTrace);
        userActivityLog.setResponseStatus(HttpResponseStatusEnum.FAILED);
        if (AppContext.getLoggedInUser() != null) {
            userActivityLog.setUserId(AppContext.getLoggedInUser().getId());
        }
        httpServletRequest.setAttribute(UserActivityLog.class.getName(), userActivityLog);
    }

    public static String getRequestHeaders(HttpServletRequest httpServletRequest) throws JsonProcessingException {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = httpServletRequest.getHeader(key);
            headerMap.put(key, value);
        }
        return CommonUtil.OBJECT_MAPPER.writeValueAsString(headerMap);
    }

}
