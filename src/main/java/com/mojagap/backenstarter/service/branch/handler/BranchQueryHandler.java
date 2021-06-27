package com.mojagap.backenstarter.service.branch.handler;

import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.model.common.RecordHolder;

import java.util.Map;

public interface BranchQueryHandler {

    RecordHolder<BranchDto> getBranches(Map<String, String> queryParams);
}
