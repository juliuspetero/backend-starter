package com.mojagap.backenstarter.service.user.handler;

import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.http.ExternalUser;
import com.mojagap.backenstarter.model.user.AppUser;

import java.util.List;
import java.util.Map;

public interface UserQueryHandler {

    RecordHolder<AppUserDto> getAppUsersByQueryParams(Map<String, String> queryParams);

    ExternalUser getExternalUserById(Integer id);

    List<AppUser> getExternalUsers();
}
