package com.mojagap.backenstarter.controller.branch;


import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.service.branch.handler.BranchCommandHandler;
import com.mojagap.backenstarter.service.branch.handler.BranchQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/branch")
public class BranchController extends BaseController {
    private final BranchQueryHandler branchQueryHandler;
    private final BranchCommandHandler branchCommandHandler;

    @Autowired
    public BranchController(BranchQueryHandler branchQueryHandler, BranchCommandHandler branchCommandHandler) {
        this.branchQueryHandler = branchQueryHandler;
        this.branchCommandHandler = branchCommandHandler;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionResponse createBranch(@RequestBody BranchDto branchDto) {
        return executeAndLogUserActivity(EntityTypeEnum.ACCOUNT, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ActionResponse response = branchCommandHandler.createBranch(branchDto);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ActionResponse updateBranch(@RequestBody BranchDto branchDto, @PathVariable("id") Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.BRANCH, ActionTypeEnum.UPDATE, (UserActivityLog log) -> {
            ActionResponse response = branchCommandHandler.updateBranch(branchDto, id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public ActionResponse closedBranch(@PathVariable("id") Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.BRANCH, ActionTypeEnum.CLOSE, (UserActivityLog log) -> {
            ActionResponse response = branchCommandHandler.closeBranch(id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(method = RequestMethod.GET)
    public RecordHolder<BranchDto> getBranches(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> branchQueryHandler.getBranches(queryParams));
    }

}
