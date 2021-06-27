package com.mojagap.backenstarter.repository.account;

import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.common.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByIdAndRecordStatus(Integer accountId, AuditEntity.RecordStatus recordStatus);
}
