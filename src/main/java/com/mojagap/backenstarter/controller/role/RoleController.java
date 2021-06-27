package com.mojagap.backenstarter.controller.role;


import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.role.PermissionDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.service.role.handler.RoleCommandHandler;
import com.mojagap.backenstarter.service.role.handler.RoleQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/role")
public class RoleController extends BaseController {

    private final RoleCommandHandler roleCommandHandler;
    private final RoleQueryHandler roleQueryHandler;

    @Autowired
    public RoleController(RoleCommandHandler roleCommandHandler, RoleQueryHandler roleQueryHandler) {
        this.roleCommandHandler = roleCommandHandler;
        this.roleQueryHandler = roleQueryHandler;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionResponse createRole(@RequestBody RoleDto roleDto) {
        return executeAndLogUserActivity(EntityTypeEnum.ROLE, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ActionResponse response = roleCommandHandler.createRole(roleDto);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{roleId}", method = RequestMethod.PUT)
    public ActionResponse updateRole(@RequestBody RoleDto roleDto, @PathVariable("roleId") Integer roleId) {
        return executeAndLogUserActivity(EntityTypeEnum.ROLE, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ActionResponse response = roleCommandHandler.updateRole(roleDto, roleId);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{roleId}", method = RequestMethod.DELETE)
    public ActionResponse removeRole(@PathVariable("roleId") Integer roleId) {
        return executeAndLogUserActivity(EntityTypeEnum.ROLE, ActionTypeEnum.REMOVE, (UserActivityLog log) -> {
            ActionResponse response = roleCommandHandler.removeRole(roleId);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(method = RequestMethod.GET)
    public RecordHolder<RoleDto> getRoles(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> roleQueryHandler.getRoles(queryParams));
    }

    @RequestMapping(path = "/permission", method = RequestMethod.GET)
    public RecordHolder<PermissionDto> getPermissions(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> roleQueryHandler.getPermissions(queryParams));
    }
}
