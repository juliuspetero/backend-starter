package com.mojagap.backenstarter.service.account.handler;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;

public interface AccountCommandHandler {

    AppUserDto createAccount(AccountDto accountDto);

    AppUserDto authenticateUser(AppUserDto appUserDto);

    ActionResponse updateAccount(AccountDto accountDto);

    ActionResponse activateAccount(Integer accountId);


}
