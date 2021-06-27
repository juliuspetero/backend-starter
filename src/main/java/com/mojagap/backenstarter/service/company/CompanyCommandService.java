package com.mojagap.backenstarter.service.company;


import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.BadRequestException;
import com.mojagap.backenstarter.infrastructure.utility.CommonUtil;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.company.CompanyType;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.company.CompanyRepository;
import com.mojagap.backenstarter.service.company.handler.CompanyCommandHandler;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyCommandService implements CompanyCommandHandler {
    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CompanyCommandService(CompanyRepository companyRepository,
                                 ModelMapper modelMapper) {
        this.companyRepository = companyRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public ActionResponse createCompany(CompanyDto companyDto) {
        companyDto.isValid();
        Account account = AppContext.getLoggedInUser().getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        List<Integer> loggedInUserCompanyIds = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
        if (companyDto.getParentCompany() == null || companyDto.getParentCompany().getId() == null) {
            PowerValidator.throwBadRequestException(String.format(ErrorMessages.ENTITY_REQUIRED, "Parent company ID"));
        }
        if (!loggedInUserCompanyIds.contains(companyDto.getParentCompany().getId())) {
            PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_PERFORM_ACTION_ON_COMPANY);
        }
        Company company = CommonUtil.copyProperties(companyDto, new Company());
        company.setCompanyType(CompanyType.valueOf(companyDto.getCompanyType()));
        Company parentCompany = companyRepository.findCompanyById(companyDto.getParentCompany().getId())
                .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Company.class.getSimpleName(), "ID")));
        company.setParentCompany(parentCompany);
        company.setAccount(account);
        Branch branch = new Branch(ApplicationConstants.DEFAULT_OFFICE_NAME, company);
        AppContext.stamp(branch);
        company.getBranches().add(branch);
        AppContext.stamp(company);
        companyRepository.save(company);
        return new ActionResponse(company.getId());
    }

    @Override
    @Transactional
    public ActionResponse updateCompany(CompanyDto companyDto, Integer id) {
        companyDto.isValid();
        Account account = AppContext.getLoggedInUser().getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        Company company = companyRepository.findCompanyById(id)
                .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Company.class.getSimpleName(), "ID")));
        List<Integer> loggedInUserCompanyIds = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
        if (!loggedInUserCompanyIds.contains(id)) {
            PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_PERFORM_ACTION_ON_COMPANY);
        }
        if (companyDto.getParentCompany() != null && companyDto.getParentCompany().getId() != null) {
            if (!loggedInUserCompanyIds.contains(companyDto.getParentCompany().getId())) {
                PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_MIGRATE_COMPANY);
            }
            Company parentCompany = companyRepository.findCompanyById(companyDto.getParentCompany().getId())
                    .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Company.class.getSimpleName(), "ID")));
            company.setParentCompany(parentCompany);
        }
        company.setEmail(companyDto.getEmail());
        company.setPhoneNumber(companyDto.getPhoneNumber());
        company.setAddress(companyDto.getAddress());
        company.setCompanyType(CompanyType.valueOf(companyDto.getCompanyType()));
        company.setName(companyDto.getName());
        company.setRegistrationDate(companyDto.getRegistrationDate());
        companyRepository.save(company);
        return new ActionResponse(id);
    }

    @Override
    @Transactional
    public ActionResponse closeCompany(Integer id) {
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Account account = loggedInUser.getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY);
        Company company = companyRepository.findCompanyById(id)
                .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Company.class.getSimpleName(), "ID")));
        List<Integer> loggedInUserCompanyIds = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
        if (!loggedInUserCompanyIds.contains(id) || loggedInUser.getCompany().getId().equals(id)) {
            PowerValidator.throwBadRequestException(ErrorMessages.NOT_PERMITTED_TO_PERFORM_ACTION_ON_COMPANY);
        }
        company.setRecordStatus(AuditEntity.RecordStatus.CLOSED);
        companyRepository.save(company);
        return new ActionResponse(id);
    }

}
