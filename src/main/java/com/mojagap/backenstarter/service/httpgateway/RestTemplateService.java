package com.mojagap.backenstarter.service.httpgateway;

import com.mojagap.backenstarter.infrastructure.logger.HttpRequestInterceptor;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.http.HttpCallLog;
import com.mojagap.backenstarter.repository.http.HttpCallLogRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RestTemplateService {

    private static final Logger LOG = Logger.getLogger(RestTemplateService.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    protected HttpServletRequest httpServletRequest;

    @Autowired
    public HttpCallLogRepository httpCallLogRepository;

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestFactory clientHttpRequestFactory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        return restTemplate;
    }

    @Bean
    @Primary
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public <R> R doHttpGet(String path, MultiValueMap<String, String> queryParams, Class<R> responseType) {
        HttpHeaders httpHeaders = getRequestHeaders();
        return executeAndLogHttpRequest(ActionTypeEnum.API_POLL_REQUEST, () -> makeApiCall(HttpMethod.GET, path, queryParams, httpHeaders, null, responseType));
    }

    public <R> R doHttpPost(String path, Object body, Class<R> responseType) {
        HttpHeaders httpHeaders = getRequestHeaders();
        return executeAndLogHttpRequest(ActionTypeEnum.API_POST_REQUEST, () -> makeApiCall(HttpMethod.POST, path, null, httpHeaders, body, responseType));
    }

    public <R> R makeApiCall(HttpMethod httpMethod, String path, MultiValueMap<String, String> queryParams, HttpHeaders headers, Object body, Class<R> responseType) {
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
        String uriString = UriComponentsBuilder.fromHttpUrl(path).queryParams(queryParams).toUriString();
        ResponseEntity<R> response = restTemplate.exchange(uriString, httpMethod, requestEntity, responseType);
        return response.getBody();
    }

    private HttpHeaders getRequestHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-Request-Source", "Desktop");
        return httpHeaders;
    }


    @SneakyThrows
    protected <R> R executeAndLogHttpRequest(ActionTypeEnum actionTypeEnum, Callable<R> callable) {
        restTemplate.setInterceptors(Collections.singletonList(new HttpRequestInterceptor()));
        try {
            return callable.call();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            HttpCallLog httpCallLog = (HttpCallLog) httpServletRequest.getAttribute(HttpCallLog.class.getName());
            if (httpCallLog != null) {
                httpCallLog.setStackTrace(ExceptionUtils.getStackTrace(ex));
            }
            throw ex;
        } finally {
            restTemplate.setInterceptors(Collections.emptyList());
            HttpCallLog httpCallLog = (HttpCallLog) httpServletRequest.getAttribute(HttpCallLog.class.getName());
            if (httpCallLog != null) {
                httpCallLog.setActionType(actionTypeEnum);
                httpCallLogRepository.saveAndFlush(httpCallLog);
            }
        }
    }
}
