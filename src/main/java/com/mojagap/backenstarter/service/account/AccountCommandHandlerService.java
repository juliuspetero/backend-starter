package com.mojagap.backenstarter.service.account;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.BadRequestException;
import com.mojagap.backenstarter.infrastructure.security.AppUserDetails;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.account.CountryCode;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.company.CompanyType;
import com.mojagap.backenstarter.model.role.Permission;
import com.mojagap.backenstarter.model.role.PermissionEnum;
import com.mojagap.backenstarter.model.role.Role;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.account.AccountRepository;
import com.mojagap.backenstarter.repository.role.PermissionRepository;
import com.mojagap.backenstarter.repository.user.AppUserRepository;
import com.mojagap.backenstarter.service.account.handler.AccountCommandHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class AccountCommandHandlerService implements AccountCommandHandler {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final PermissionRepository permissionRepository;
    private final AppUserRepository appUserRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    protected final HttpServletResponse httpServletResponse;

    @Autowired
    public AccountCommandHandlerService(PasswordEncoder passwordEncoder, AccountRepository accountRepository, PermissionRepository permissionRepository,
                                        AppUserRepository appUserRepository, ModelMapper modelMapper, AuthenticationManager authenticationManager, HttpServletResponse httpServletResponse) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.permissionRepository = permissionRepository;
        this.appUserRepository = appUserRepository;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    @Transactional
    public AppUserDto createAccount(AccountDto accountDto) {
        accountDto.isValid();
        PowerValidator.notEmpty(accountDto.getUsers(), ErrorMessages.USER_REQUIRED_WHEN_CREATING_ACCOUNT);
        AppUserDto appUserDto = accountDto.getUsers().get(0);
        appUserDto.isValid();
        PowerValidator.validPassword(appUserDto.getPassword(), ErrorMessages.INVALID_PASSWORD);
        AppUser appUser = new AppUser(appUserDto);
        AppContext.stamp(appUser);
        appUser.setModifiedBy(appUser);
        appUser.setCreatedBy(appUser);
        appUser.setVerified(Boolean.FALSE);
        String rawPassword = appUserDto.getPassword();
        appUser.setPassword(passwordEncoder.encode(appUserDto.getPassword()));

        Account account = new Account(accountDto);
        account.setRecordStatus(AuditEntity.RecordStatus.INACTIVE);
        AppContext.stamp(account);
        AppContext.stamp(account);
        account.setCreatedBy(appUser);
        account.setModifiedBy(appUser);
        account.getAppUsers().add(appUser);
        appUser.setAccount(account);

        AccountType accountType = account.getAccountType();
        switch (accountType) {
            case INDIVIDUAL -> {
                account.setAddress(appUserDto.getAddress());
                account.setEmail(appUserDto.getEmail());
                account.setContactPhoneNumber(appUserDto.getPhoneNumber());
                account.setName(appUserDto.getFirstName() + " " + appUserDto.getLastName());
            }
            case COMPANY -> {
                // accountDto.isValidCompany();
                Company company = setCompanyProps(accountDto, account);
                company.setCreatedBy(appUser);
                company.setModifiedBy(appUser);
                Branch branch = new Branch("Head Office", company);
                branch.setParentBranch(branch);
                AppContext.stamp(branch);
                branch.setCreatedBy(appUser);
                branch.setModifiedBy(appUser);
                company.getBranches().add(branch);
                account.getCompanies().add(company);
                appUser.setCompany(company);
                appUser.setRole(createSuperUserRole(account));
                appUser.setBranch(branch);
            }
            case PARTNER, BACK_OFFICE -> throw new UnsupportedOperationException("You cannot create a backoffice or partner account at the moment");
            default -> throw new UnsupportedOperationException("The account type provided is not accepted here");
        }
        accountRepository.save(account);
        appUserDto.setPassword(rawPassword);
        return authenticateUser(appUserDto);
    }

    @Override
    public AppUserDto authenticateUser(AppUserDto appUserDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(appUserDto.getEmail(), appUserDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUser appUser = ((AppUserDetails) authentication.getPrincipal()).getAppUser();
        String authenticationToken = generateAuthenticationToken(appUser);
        appUserDto.setAuthentication(authenticationToken);
        BeanUtils.copyProperties(appUser, appUserDto);
        appUserDto.setPassword(null);

        if (EnumSet.of(AccountType.BACK_OFFICE, AccountType.COMPANY).contains(appUser.getAccount().getAccountType())) {
            RoleDto roleDto = modelMapper.map(appUser.getRole(), RoleDto.class);
            appUserDto.setRole(roleDto);
        }
        Company company = appUser.getCompany();
        if (company != null) {
            appUserDto.setCompany(new CompanyDto(company.getId(), company.getName(), company.getCompanyType().name(), company.getRecordStatus().name()));
        }
        Branch branch = appUser.getBranch();
        if (branch != null) {
            appUserDto.setBranch(new BranchDto(branch.getId(), branch.getName(), branch.getCreatedOn(), branch.getRecordStatus().name()));
        }
        Account account = appUser.getAccount();
        appUserDto.setAccount(new AccountDto(account.getId(), account.getAccountType().name(), account.getCountryCode().name()));
        httpServletResponse.setHeader(ApplicationConstants.AUTHENTICATION_HEADER_NAME, authenticationToken);
        return appUserDto;
    }

    public static String generateAuthenticationToken(AppUser appUser) {
        String expirationTime = ApplicationConstants.JWT_EXPIRATION_TIME_IN_MINUTES;
        Date expiryDate = new Date(System.currentTimeMillis() + Long.parseLong(expirationTime) * 60 * 1000);
        String secretKey = Base64.getEncoder().encodeToString(ApplicationConstants.JWT_SECRET_KEY.getBytes());
        Claims claims = Jwts.claims().setSubject(appUser.getEmail());
        claims.put(ApplicationConstants.APP_USER_ID, appUser.getId());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secretKey).setExpiration(expiryDate).compact();
    }

    @Override
    public ActionResponse updateAccount(AccountDto accountDto) {
        accountDto.isValid();
        AppUser loggedInUser = appUserRepository.getById(AppContext.getLoggedInUser().getId());
        Account account = loggedInUser.getAccount();
        account.setCountryCode(CountryCode.valueOf(accountDto.getCountryCode()));
        AccountType accountType = account.getAccountType();
        switch (accountType) {
            case INDIVIDUAL -> {
                if (accountDto.getAccountType().equals(AccountType.COMPANY.name())) {
                    account.setAccountType(AccountType.valueOf(accountDto.getAccountType()));
                    Company company = setCompanyProps(accountDto, account);
                    Branch branch = new Branch("Head Office", company);
                    branch.setParentBranch(branch);
                    AppContext.stamp(branch);
                    company.getBranches().add(branch);
                    account.getCompanies().add(company);
                    loggedInUser.setCompany(company);
                    loggedInUser.setRole(createSuperUserRole(account));
                    loggedInUser.setBranch(branch);
                    loggedInUser.setAccount(account);
                    account.getAppUsers().add(loggedInUser);
                }
            }
            case COMPANY -> setAccountProperties(account, accountDto);
            default -> throw new UnsupportedOperationException(ErrorMessages.ACCOUNT_TYPE_NOT_PERMITTED);
        }
        AppContext.stamp(account);
        accountRepository.save(account);
        return new ActionResponse(account.getId());
    }

    private Company setCompanyProps(AccountDto accountDto, Account account) {
        PowerValidator.notEmpty(accountDto.getCompanies(), ErrorMessages.COMPANY_DETAILS_REQUIRED);
        CompanyDto companyDto = accountDto.getCompanies().get(0);
        companyDto.isValid();
        account.setAddress(companyDto.getAddress());
        account.setEmail(companyDto.getEmail());
        account.setContactPhoneNumber(companyDto.getPhoneNumber());
        account.setName(companyDto.getName());
        Company company = new Company(companyDto);
        company.setCompanyType(CompanyType.valueOf(companyDto.getCompanyType()));
        company.setParentCompany(company);
        company.setAccount(account);
        AppContext.stamp(company);
        return company;
    }

    @Override
    public ActionResponse activateAccount(Integer accountId) {
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Account.class.getSimpleName(), "ID")));
        PowerValidator.isFalse(AuditEntity.RecordStatus.ACTIVE.equals(account.getRecordStatus()), "Account is already active");
        account.setRecordStatus(AuditEntity.RecordStatus.ACTIVE);
        AppContext.stamp(account);
        account.setActivatedBy(loggedInUser);
        account.setActivatedOn(DateUtil.now());
        accountRepository.saveAndFlush(account);
        return new ActionResponse(accountId);
    }

    private void setAccountProperties(Account account, AccountDto accountDto) {
        accountDto.isValidCompany();
        account.setName(accountDto.getName());
        account.setAddress(accountDto.getAddress());
        account.setEmail(accountDto.getEmail());
        account.setContactPhoneNumber(accountDto.getContactPhoneNumber());
    }

    private Role createSuperUserRole(Account account) {
        Permission superPermission = permissionRepository.findOneByName(PermissionEnum.SUPER_PERMISSION.name());
        return new Role(ApplicationConstants.DEFAULT_ROLE_NAME, ApplicationConstants.DEFAULT_ROLE_DESCRIPTION, account, Collections.singletonList(superPermission));
    }
}
