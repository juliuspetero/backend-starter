package com.mojagap.backenstarter.service.account.handler;

import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.model.common.RecordHolder;

import java.util.Map;

public interface AccountQueryHandler {

    RecordHolder<AccountDto> getAccounts(Map<String, String> queryParams);

}
