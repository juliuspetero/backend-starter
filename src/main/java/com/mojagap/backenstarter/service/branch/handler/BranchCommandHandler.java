package com.mojagap.backenstarter.service.branch.handler;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.branch.BranchDto;

public interface BranchCommandHandler {

    ActionResponse createBranch(BranchDto branchDto);

    ActionResponse updateBranch(BranchDto branchDto, Integer id);

    ActionResponse closeBranch(Integer id);
}
