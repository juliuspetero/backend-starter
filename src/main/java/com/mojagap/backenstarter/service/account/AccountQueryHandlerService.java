package com.mojagap.backenstarter.service.account;

import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.service.account.handler.AccountQueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AccountQueryHandlerService implements AccountQueryHandler {

    @Override
    public RecordHolder<AccountDto> getAccounts(Map<String, String> queryParams) {
        return new RecordHolder<>(1, List.of(new AccountDto()));
    }
}
