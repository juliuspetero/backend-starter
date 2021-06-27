package com.mojagap.backenstarter.controller.company;


import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.controller.BaseController;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.user.UserActivityLog;
import com.mojagap.backenstarter.service.company.handler.CompanyCommandHandler;
import com.mojagap.backenstarter.service.company.handler.CompanyQueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/company")
public class CompanyController extends BaseController {
    private final CompanyQueryHandler companyQueryHandler;
    private final CompanyCommandHandler companyCommandHandler;

    @Autowired
    public CompanyController(CompanyQueryHandler companyQueryHandler, CompanyCommandHandler companyCommandHandler) {
        this.companyQueryHandler = companyQueryHandler;
        this.companyCommandHandler = companyCommandHandler;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ActionResponse createCompany(@RequestBody CompanyDto companyDto) {
        return executeAndLogUserActivity(EntityTypeEnum.COMPANY, ActionTypeEnum.CREATE, (UserActivityLog log) -> {
            ActionResponse response = companyCommandHandler.createCompany(companyDto);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public ActionResponse updateCompany(@RequestBody CompanyDto companyDto, @PathVariable Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.COMPANY, ActionTypeEnum.UPDATE, (UserActivityLog log) -> {
            ActionResponse response = companyCommandHandler.updateCompany(companyDto, id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public ActionResponse closedCompany(@PathVariable Integer id) {
        return executeAndLogUserActivity(EntityTypeEnum.COMPANY, ActionTypeEnum.CLOSE, (UserActivityLog log) -> {
            ActionResponse response = companyCommandHandler.closeCompany(id);
            log.setEntityId(response.resourceId());
            return response;
        });
    }

    @RequestMapping(method = RequestMethod.GET)
    public RecordHolder<CompanyDto> getBranches(@RequestParam Map<String, String> queryParams) {
        return executeHttpGet(() -> companyQueryHandler.getCompanies(queryParams));
    }
}
