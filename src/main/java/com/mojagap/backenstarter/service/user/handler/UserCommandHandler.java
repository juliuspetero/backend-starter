package com.mojagap.backenstarter.service.user.handler;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.model.http.ExternalUser;

public interface UserCommandHandler {

    ActionResponse createUser(AppUserDto appUserDto);

    ActionResponse updateUser(AppUserDto appUserDto, Integer id);

    ActionResponse removeUser(Integer userId);

    ExternalUser createExternalUser(ExternalUser externalUser);
}
