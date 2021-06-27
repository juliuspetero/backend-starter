package com.mojagap.backenstarter.service.company.handler;

import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.model.common.RecordHolder;

import java.util.Map;

public interface CompanyQueryHandler {

    RecordHolder<CompanyDto> getCompanies(Map<String, String> queryParams);
}
