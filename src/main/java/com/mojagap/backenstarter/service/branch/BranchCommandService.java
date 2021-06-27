package com.mojagap.backenstarter.service.branch;


import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.BadRequestException;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.branch.BranchRepository;
import com.mojagap.backenstarter.service.branch.handler.BranchCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchCommandService implements BranchCommandHandler {
    private final BranchRepository branchRepository;

    @Autowired
    public BranchCommandService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }


    @Override
    public ActionResponse createBranch(BranchDto branchDto) {
        branchDto.isValid();
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Account account = loggedInUser.getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        Branch parentBranch = getParentBranch(branchDto);
        Company company = loggedInUser.getCompany();
        Branch branch = new Branch(branchDto.getName(), company);
        branch.setParentBranch(parentBranch);
        AppContext.stamp(branch);
        company.getBranches().add(branch);
        branchRepository.save(branch);
        return new ActionResponse(branch.getId());
    }

    @Override
    public ActionResponse updateBranch(BranchDto branchDto, Integer id) {
        Account account = AppContext.getLoggedInUser().getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        branchDto.isValid();
        PowerValidator.notNull(id, String.format(ErrorMessages.ENTITY_REQUIRED, "Branch ID"));
        Branch branch = branchRepository.findById(id).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, "Branch", "ID")));
        Branch parentBranch = getParentBranch(branchDto);
        branch.setParentBranch(parentBranch);
        branch.setName(branchDto.getName());
        AppContext.stamp(branch);
        branchRepository.save(branch);
        return new ActionResponse(id);
    }

    private Branch getParentBranch(BranchDto branchDto) {
        List<Integer> loggedInUserBranchIds = AppContext.getBranchesOfLoggedInUser().stream().map(Branch::getId).collect(Collectors.toList());
        Branch parentBranch = branchRepository.findById(branchDto.getParentBranch().getId()).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, "Branch", "ID")));
        if (!loggedInUserBranchIds.contains(branchDto.getParentBranch().getId())) {
            PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_CREATE_BRANCH_UNDER_PARENT);
        }
        return parentBranch;
    }

    @Override
    public ActionResponse closeBranch(Integer id) {
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Account account = loggedInUser.getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        Branch branch = branchRepository.findById(id).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, "Branch", "ID")));
        List<Integer> loggedInUserBranchIds = AppContext.getBranchesOfLoggedInUser().stream().map(Branch::getId).collect(Collectors.toList());
        if (!loggedInUserBranchIds.contains(id) || loggedInUser.getBranch().getId().equals(id)) {
            PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_CLOSE_COMPANY);
        }
        branch.setRecordStatus(AuditEntity.RecordStatus.CLOSED);
        branchRepository.save(branch);
        return new ActionResponse(id);
    }
}
