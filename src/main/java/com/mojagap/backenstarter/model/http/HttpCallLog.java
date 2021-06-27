package com.mojagap.backenstarter.model.http;

import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.BaseEntity;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Setter
@Entity(name = "http_call_log")
@NoArgsConstructor
public class HttpCallLog extends BaseEntity {
    private String requestUrl;
    private String requestMethod;
    private String requestHeaders;
    private String requestBody;

    private String responseBody;
    private String responseHeaders;
    private HttpResponseStatusEnum responseStatus;
    private Integer responseStatusCode;
    private String stackTrace;

    private ActionTypeEnum actionType;
    private Date createdOn;
    private Integer duration;

    @Column(name = "request_url")
    public String getRequestUrl() {
        return requestUrl;
    }

    @Column(name = "request_method")
    public String getRequestMethod() {
        return requestMethod;
    }

    @Column(name = "request_headers")
    public String getRequestHeaders() {
        return requestHeaders;
    }

    @Column(name = "request_body")
    public String getRequestBody() {
        return requestBody;
    }

    @Column(name = "response_body")
    public String getResponseBody() {
        return responseBody;
    }

    @Column(name = "response_headers")
    public String getResponseHeaders() {
        return responseHeaders;
    }

    @Column(name = "response_status")
    @Enumerated(EnumType.STRING)
    public HttpResponseStatusEnum getResponseStatus() {
        return responseStatus;
    }

    @Column(name = "response_status_code")
    public Integer getResponseStatusCode() {
        return responseStatusCode;
    }

    @Column(name = "stack_trace")
    public String getStackTrace() {
        return stackTrace;
    }

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    public ActionTypeEnum getActionType() {
        return actionType;
    }

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedOn() {
        return createdOn;
    }

    @Column(name = "duration")
    public Integer getDuration() {
        return duration;
    }

}
