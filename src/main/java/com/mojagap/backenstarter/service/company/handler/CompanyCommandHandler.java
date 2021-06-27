package com.mojagap.backenstarter.service.company.handler;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.company.CompanyDto;

public interface CompanyCommandHandler {

    ActionResponse createCompany(CompanyDto companyDto);

    ActionResponse updateCompany(CompanyDto companyDto, Integer id);

    ActionResponse closeCompany(Integer id);
}
