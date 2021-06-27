package com.mojagap.backenstarter.model.user;


import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.BaseEntity;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.http.HttpResponseStatusEnum;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Entity(name = "user_activity_log")
@NoArgsConstructor
public class UserActivityLog extends BaseEntity {
    private String requestUrl;
    private String requestMethod;
    private String requestHeaders;
    private String requestBody;

    private String responseBody;
    private String responseHeaders;
    private HttpResponseStatusEnum responseStatus;
    private Integer responseStatusCode;
    private String stackTrace;

    private EntityTypeEnum entityType;
    private Integer entityId;
    private ActionTypeEnum actionType;
    private Date createdOn;
    private Integer duration;

    private Integer userId;
    private String remoteIpAddress;
    private Integer platformType;

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

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    public EntityTypeEnum getEntityType() {
        return entityType;
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

    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    @Column(name = "remote_ip_address")
    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    @Column(name = "platform_type")
    public Integer getPlatformType() {
        return platformType;
    }

    @Column(name = "entity_id")
    public Integer getEntityId() {
        return entityId;
    }
}
