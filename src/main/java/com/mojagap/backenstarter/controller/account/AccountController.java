package com.mojagap.backenstarter.controller.account;

import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.service.account.handler.AccountCommandHandler;
import com.mojagap.backenstarter.service.account.handler.AccountQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/v1/account")
public class AccountController extends BaseController {
    private final AccountCommandHandler accountCommandHandler;
    private final AccountQueryHandler accountQueryHandler;

    @Autowired
    public AccountController(AccountCommandHandler accountCommandHandler, AccountQueryHandler accountQueryHandler) {
        this.accountCommandHandler = accountCommandHandler;
        this.accountQueryHandler = accountQueryHandler;
    }

    @RequestMapping(method = RequestMethod.POST)
    public AppUserDto createAccount(@RequestBody AccountDto accountDto) {
        return executeAndLogUserActivity(EntityTypeEnum.ACCOUNT, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            AppUserDto response = accountCommandHandler.createAccount(accountDto);
            log.setEntityId(response.getAccount().getAccountId());
            return response;
        });
    }

    @RequestMapping(path = "/authenticate", method = RequestMethod.GET)
    public AppUserDto authenticateUser(@RequestBody AppUserDto appUserDto) {
        return executeHttpGet(() -> accountCommandHandler.authenticateUser(appUserDto));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ActionResponse updateAccount(@RequestBody AccountDto accountDto) {
        return executeAndLogUserActivity(EntityTypeEnum.ACCOUNT, ActionTypeEnum.UPDATE, (UserActivityLog log) -> {
            ActionResponse actionResponse = accountCommandHandler.updateAccount(accountDto);
            log.setEntityId(actionResponse.resourceId());
            return actionResponse;
        });
    }

    @RequestMapping(path = "/activate/{id}", method = RequestMethod.POST)
    public ActionResponse activateAccount(@PathVariable("id") Integer accountId) {
        return executeAndLogUserActivity(EntityTypeEnum.ACCOUNT, ActionTypeEnum.APPROVE, (UserActivityLog log) -> {
            ActionResponse actionResponse = accountCommandHandler.activateAccount(accountId);
            log.setEntityId(actionResponse.resourceId());
            return actionResponse;
        });
    }

    @RequestMapping(method = RequestMethod.GET)
    public RecordHolder<AccountDto> getAccounts(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> accountQueryHandler.getAccounts(queryParams));
    }
}
