package com.mojagap.backenstarter.service.role.handler;

import com.mojagap.backenstarter.dto.role.PermissionDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.model.common.RecordHolder;

import java.util.Map;

public interface RoleQueryHandler {

    RecordHolder<RoleDto> getRoles(Map<String, String> queryParams);

    RecordHolder<PermissionDto> getPermissions(Map<String, String> queryParams);
}
