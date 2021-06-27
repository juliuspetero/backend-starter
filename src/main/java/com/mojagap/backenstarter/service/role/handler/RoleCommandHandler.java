package com.mojagap.backenstarter.service.role.handler;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.role.RoleDto;

public interface RoleCommandHandler {

    ActionResponse createRole(RoleDto roleDto);

    ActionResponse updateRole(RoleDto roleDto, Integer roleId);

    ActionResponse removeRole(Integer roleId);

}
