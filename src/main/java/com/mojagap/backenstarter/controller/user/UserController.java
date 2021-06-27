package com.mojagap.backenstarter.controller.user;

import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.http.ExternalUser;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.service.user.handler.UserCommandHandler;
import com.mojagap.backenstarter.service.user.handler.UserQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/user")
public class UserController extends BaseController {

    private final UserCommandHandler userCommandHandler;
    private final UserQueryHandler userQueryHandler;

    @Autowired
    public UserController(UserCommandHandler userCommandHandler, UserQueryHandler userQueryHandler) {
        this.userCommandHandler = userCommandHandler;
        this.userQueryHandler = userQueryHandler;
    }

    @GetMapping
    public RecordHolder<AppUserDto> getAppUsers(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> userQueryHandler.getAppUsersByQueryParams(queryParams));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionResponse createUser(@RequestBody AppUserDto appUserDto) {
        return executeAndLogUserActivity(EntityTypeEnum.USER, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ActionResponse response = userCommandHandler.createUser(appUserDto);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ActionResponse updateUser(@RequestBody AppUserDto appUserDto, @PathVariable Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.USER, ActionTypeEnum.UPDATE, (UserActivityLog log) -> {
            ActionResponse response = userCommandHandler.updateUser(appUserDto, id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public ActionResponse removeUser(@PathVariable Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.USER, ActionTypeEnum.UPDATE, (UserActivityLog log) -> {
            ActionResponse response = userCommandHandler.removeUser(id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }


    @RequestMapping(path = "/external/{id}", method = RequestMethod.GET)
    public ExternalUser getExternalUser(@PathVariable Integer id) {
        return executeHttpGet(() -> userQueryHandler.getExternalUserById(id));
    }

    @RequestMapping(path = "/external", method = RequestMethod.GET)
    public ExternalUser createExternalUser(@RequestBody ExternalUser externalUser) {
        return executeAndLogUserActivity(EntityTypeEnum.USER, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ExternalUser response = userCommandHandler.createExternalUser(externalUser);
            log.setEntityId(response.getId());
            return response;
        });
    }

    @GetMapping("/external/all")
    public List<AppUser> getExternalUsers() {
        return executeHttpGet(userQueryHandler::getExternalUsers);
    }

}
