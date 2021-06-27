package com.mojagap.backenstarter.service.user;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.BadRequestException;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.http.ExternalUser;
import com.mojagap.backenstarter.model.role.Role;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.branch.BranchRepository;
import com.mojagap.backenstarter.repository.company.CompanyRepository;
import com.mojagap.backenstarter.repository.role.RoleRepository;
import com.mojagap.backenstarter.repository.user.AppUserRepository;
import com.mojagap.backenstarter.service.httpgateway.RestTemplateService;
import com.mojagap.backenstarter.service.user.handler.UserCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCommandService implements UserCommandHandler {
    private final CompanyRepository companyRepository;
    private final AppUserRepository appUserRepository;
    private final RestTemplateService restTemplateService;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;
    protected final HttpServletResponse httpServletResponse;
    private final RoleRepository roleRepository;

    @Autowired
    public UserCommandService(CompanyRepository companyRepository, AppUserRepository appUserRepository, RestTemplateService restTemplateService, PasswordEncoder passwordEncoder,
                              BranchRepository branchRepository, HttpServletResponse httpServletResponse, RoleRepository roleRepository) {
        this.companyRepository = companyRepository;
        this.appUserRepository = appUserRepository;
        this.restTemplateService = restTemplateService;
        this.passwordEncoder = passwordEncoder;
        this.branchRepository = branchRepository;
        this.httpServletResponse = httpServletResponse;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public ActionResponse createUser(AppUserDto appUserDto) {
        appUserDto.isValid();
        AppUser loggedInUser = AppContext.getLoggedInUser();
        AccountType accountType = loggedInUser.getAccount().getAccountType();
        PowerValidator.isTrue(EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(accountType), String.format(ErrorMessages.ACCOUNT_TYPE_NOT_PERMITTED, accountType));
        PowerValidator.notNull(appUserDto.getRole().getId(), String.format(ErrorMessages.ENTITY_REQUIRED, "role ID"));
        Role role = roleRepository.findByIdAndAccountId(appUserDto.getRole().getId(), loggedInUser.getAccount().getId()).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Role.class.getSimpleName(), "ID")));
        AppUser appUser = new AppUser(appUserDto);
        appUser.setRole(role);
        appUser.setAccount(loggedInUser.getAccount());
        AppContext.stamp(appUser);
        appUser.setPassword(passwordEncoder.encode(appUserDto.getPassword()));
        if (AccountType.COMPANY.equals(accountType)) {
            updateCompanyDetails(appUserDto, appUser);
        }
        appUserRepository.save(appUser);
        return new ActionResponse(appUser.getId());
    }

    @Override
    public ActionResponse updateUser(AppUserDto appUserDto, Integer id) {
        appUserDto.isValid();
        Account account = AppContext.getLoggedInUser().getAccount();
        AppUser appUser = appUserRepository.findByIdAndAcountId(id, account.getId()).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, "User", "ID")));
        PowerValidator.isTrue(EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(account.getAccountType()), String.format(ErrorMessages.ACCOUNT_TYPE_NOT_PERMITTED, account.getAccountType()));
        PowerValidator.notNull(appUserDto.getRole().getId(), String.format(ErrorMessages.ENTITY_REQUIRED, "role ID"));
        Role role = roleRepository.findByIdAndAccountId(appUserDto.getRole().getId(), account.getId()).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Role.class.getSimpleName(), "ID")));
        appUser.setRole(role);
        appUser.setAccount(account);
        AppContext.stamp(appUser);
        if (appUserDto.getPassword() != null) {
            appUser.setPassword(passwordEncoder.encode(appUserDto.getPassword()));
        }
        if (AccountType.COMPANY.equals(account.getAccountType())) {
            updateCompanyDetails(appUserDto, appUser);
        }
        appUserRepository.save(appUser);
        return new ActionResponse(appUser.getId());
    }

    private void updateCompanyDetails(AppUserDto appUserDto, AppUser appUser) {
        PowerValidator.notNull(appUserDto.getCompany().getId(), String.format(ErrorMessages.ENTITY_REQUIRED, "company ID"));
        Company company = companyRepository.findCompanyById(appUserDto.getCompany().getId())
                .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Company.class.getSimpleName(), "ID")));
        List<Integer> loggedInUserCompanies = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
        if (!loggedInUserCompanies.contains(appUserDto.getCompany().getId())) {
            PowerValidator.throwBadRequestException(ErrorMessages.CANNOT_CREATE_USER_UNDER_SUB_COMPANY);
        }
        appUser.setCompany(company);
        PowerValidator.notNull(appUserDto.getBranch().getId(), String.format(ErrorMessages.ENTITY_REQUIRED, "branch ID"));
        Branch branch = branchRepository.findById(appUserDto.getBranch().getId())
                .orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Branch.class.getSimpleName(), "ID")));
        List<Integer> loggedInUserBranches = AppContext.getBranchesOfLoggedInUser().stream().map(Branch::getId).collect(Collectors.toList());
        if (!loggedInUserBranches.contains(appUserDto.getBranch().getId())) {
            PowerValidator.throwBadRequestException(ErrorMessages.CANNOT_CREATE_USER_IN_BRANCH);
        }
        appUser.setBranch(branch);
    }

    @Override
    public ActionResponse removeUser(Integer userId) {
        PowerValidator.notNull(userId, String.format(ErrorMessages.ENTITY_REQUIRED, "User ID"));
        Account account = AppContext.getLoggedInUser().getAccount();
        AccountType accountType = account.getAccountType();
        AppUser appUser = appUserRepository.findByIdAndAcountId(userId, account.getId()).orElseThrow(() ->
                new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, "User", "ID")));
        PowerValidator.isTrue(EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(accountType), String.format(ErrorMessages.ACCOUNT_TYPE_NOT_PERMITTED, accountType));
        appUser.setRecordStatus(AuditEntity.RecordStatus.DELETED);
        return new ActionResponse(userId);
    }

    @Override
    public ExternalUser createExternalUser(ExternalUser externalUser) {
        return restTemplateService.doHttpPost(ApplicationConstants.BANK_TRANSFER_BASE_URL + "/users", externalUser, ExternalUser.class);

    }
}
