package com.mojagap.backenstarter.service.user;


import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.dto.user.UserSqlResultSet;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ApplicationConstants;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.security.AppUserDetails;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.http.ExternalUser;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.model.user.IdentificationEnum;
import com.mojagap.backenstarter.repository.user.AppUserRepository;
import com.mojagap.backenstarter.service.httpgateway.RestTemplateService;
import com.mojagap.backenstarter.service.user.handler.UserQueryHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserQueryService implements UserDetailsService, UserQueryHandler {
    private final AppUserRepository appUserRepository;
    private final RestTemplateService restTemplateService;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public UserQueryService(AppUserRepository appUserRepository, RestTemplateService restTemplateService, NamedParameterJdbcTemplate jdbcTemplate) {
        this.appUserRepository = appUserRepository;
        this.restTemplateService = restTemplateService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @SneakyThrows(ParseException.class)
    public RecordHolder<AppUserDto> getAppUsersByQueryParams(Map<String, String> queryParams) {
        Account account = AppContext.getLoggedInUser().getAccount();
        Arrays.asList(AppUserQueryParams.values()).forEach(param -> queryParams.putIfAbsent(param.getValue(), null));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(queryParams);
        String appUserQuery;
        if (AccountType.COMPANY.equals(account.getAccountType())) {
            appUserQuery = companyUserQuery();
            List<Integer> branches = AppContext.getBranchesOfLoggedInUser().stream().map(Branch::getId).collect(Collectors.toList());
            List<Integer> companies = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
            mapSqlParameterSource.addValue(AppUserQueryParams.BRANCH_IDS.getValue(), branches);
            mapSqlParameterSource.addValue(AppUserQueryParams.COMPANY_IDS.getValue(), companies);
            mapSqlParameterSource.addValue(AppUserQueryParams.ACCOUNT_ID.getValue(), account.getId());
        } else {
            appUserQuery = backofficeUserQuery();
            if (queryParams.get(AppUserQueryParams.ACCOUNT_ID.getValue()) == null) {
                mapSqlParameterSource.addValue(AppUserQueryParams.ACCOUNT_ID.getValue(), account.getId());
            }
        }

        if (queryParams.get(AppUserQueryParams.DATE_OF_BIRTH.getValue()) != null) {
            Date dateOfBirth = DateUtil.DefaultDateFormat().parse(queryParams.get(AppUserQueryParams.DATE_OF_BIRTH.getValue()));
            mapSqlParameterSource.addValue(AppUserQueryParams.DATE_OF_BIRTH.getValue(), dateOfBirth, Types.BOOLEAN);
        }
        if (queryParams.get(AppUserQueryParams.VERIFIED.getValue()) != null) {
            mapSqlParameterSource.addValue(AppUserQueryParams.VERIFIED.getValue(), Boolean.parseBoolean(queryParams.get(AppUserQueryParams.VERIFIED.getValue())), Types.BOOLEAN);
        }
        Integer limit = queryParams.get(AppUserQueryParams.LIMIT.getValue()) != null ? Integer.parseInt(queryParams.get(AppUserQueryParams.LIMIT.getValue())) : Integer.MAX_VALUE;
        mapSqlParameterSource.addValue(AppUserQueryParams.LIMIT.getValue(), limit, Types.INTEGER);
        Integer offset = queryParams.get(AppUserQueryParams.OFFSET.getValue()) != null ? Integer.parseInt(queryParams.get(AppUserQueryParams.OFFSET.getValue())) : 0;
        mapSqlParameterSource.addValue(AppUserQueryParams.OFFSET.getValue(), offset, Types.INTEGER);
        List<UserSqlResultSet> sqlResultSets = jdbcTemplate.query(appUserQuery, mapSqlParameterSource, new AppUserMapper());
        List<AppUserDto> appUserDtos = sqlResultSets.stream().map(this::fromSqlResultSet).collect(Collectors.toList());
        return new RecordHolder<>(appUserDtos.size(), appUserDtos);
    }

    private AppUserDto fromSqlResultSet(UserSqlResultSet resultSet) {
        AppUserDto appUserDto = new AppUserDto();
        BeanUtils.copyProperties(resultSet, appUserDto);
        appUserDto.setAccount(new AccountDto(resultSet.getAccountId(), resultSet.getAccountType(), resultSet.getCountryCode()));
        if (resultSet.getCompanyId() != 0 && resultSet.getCompanyId() != null) {
            appUserDto.setCompany(new CompanyDto(resultSet.getCompanyId(), resultSet.getCompanyName(), resultSet.getCompanyType(), resultSet.getCompanyStatus()));
        }
        if (resultSet.getBranchId() != 0 && resultSet.getBranchId() != null) {
            appUserDto.setBranch(new BranchDto(resultSet.getBranchId(), resultSet.getBranchName(), resultSet.getBranchOpeningDate(), resultSet.getBranchStatus()));
        }
        if (resultSet.getRoleId() != 0 && resultSet.getRoleId() != null) {
            appUserDto.setRole(new RoleDto(resultSet.getRoleId(), resultSet.getRoleName(), resultSet.getRoleDescription(), resultSet.getRoleStatus()));
        }
        return appUserDto;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findOneByEmailAndRecordStatus(s, AuditEntity.RecordStatus.ACTIVE);
        PowerValidator.notNull(appUser, ErrorMessages.INVALID_SECURITY_CREDENTIAL);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (appUser.getRole() != null) {
            appUser.getRole().getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getName())).forEach(authorities::add);
        }
        return new AppUserDetails(appUser, authorities);
    }

    @Override
    public ExternalUser getExternalUserById(Integer id) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.set("name", "Peter");
        queryParams.set("age", "56");
        queryParams.set("sex", "MALE");
        return restTemplateService.doHttpGet(ApplicationConstants.BANK_TRANSFER_BASE_URL + "/users/" + id, queryParams, ExternalUser.class);
    }

    @Override
    public List<AppUser> getExternalUsers() {
        ExternalUser[] externalUsers = restTemplateService.doHttpGet(ApplicationConstants.BANK_TRANSFER_BASE_URL + "/users", null, ExternalUser[].class);
        List<AppUser> appUsers = List.of(externalUsers).stream().map(x -> {
            AppUser appUser = new AppUser();
            appUser.setId(x.getId());
            appUser.setLastName(x.getName());
            appUser.setFirstName(x.getUsername());
            appUser.setPhoneNumber(x.getPhone());
            appUser.setEmail(x.getEmail());
            appUser.setAddress("XXXXXXXXX");
            appUser.setPassword("PASSWORD");
            appUser.setVerified(Boolean.FALSE);
            appUser.setDateOfBirth(DateUtil.now());
            appUser.setIdNumber(IdentificationEnum.NATIONAL_ID.name());
            AppContext.stamp(appUser);
            return appUser;
        }).collect(Collectors.toList());
        appUserRepository.saveAllAndFlush(appUsers);
        return appUsers;
    }


    private static final class AppUserMapper implements RowMapper<UserSqlResultSet> {

        @Override
        public UserSqlResultSet mapRow(ResultSet resultSet, int i) throws SQLException {
            UserSqlResultSet userSqlResultSet = new UserSqlResultSet();
            userSqlResultSet.setId(resultSet.getInt(AppUserQueryParams.ID.getValue()));
            userSqlResultSet.setLastName(resultSet.getString(AppUserQueryParams.LAST_NAME.getValue()));
            userSqlResultSet.setFirstName(resultSet.getString(AppUserQueryParams.FIRST_NAME.getValue()));
            userSqlResultSet.setAddress(resultSet.getString(AppUserQueryParams.ADDRESS.getValue()));
            userSqlResultSet.setEmail(resultSet.getString(AppUserQueryParams.EMAIL.getValue()));
            userSqlResultSet.setDateOfBirth(resultSet.getDate(AppUserQueryParams.DATE_OF_BIRTH.getValue()));
            userSqlResultSet.setIdNumber(resultSet.getString(AppUserQueryParams.ID_NUMBER.getValue()));
            userSqlResultSet.setVerified(resultSet.getBoolean(AppUserQueryParams.VERIFIED.getValue()));
            userSqlResultSet.setUserStatus(resultSet.getString(AppUserQueryParams.USER_STATUS.getValue()));
            userSqlResultSet.setCompanyId(resultSet.getInt(AppUserQueryParams.COMPANY_ID.getValue()));
            userSqlResultSet.setCompanyName(resultSet.getString(AppUserQueryParams.COMPANY_NAME.getValue()));
            userSqlResultSet.setCompanyStatus(resultSet.getString(AppUserQueryParams.COMPANY_STATUS.getValue()));
            userSqlResultSet.setAccountId(resultSet.getInt(AppUserQueryParams.ACCOUNT_ID.getValue()));
            userSqlResultSet.setAccountType(resultSet.getString(AppUserQueryParams.ACCOUNT_TYPE.getValue()));
            userSqlResultSet.setBranchId(resultSet.getInt(AppUserQueryParams.BRANCH_ID.getValue()));
            userSqlResultSet.setBranchName(resultSet.getString(AppUserQueryParams.BRANCH_NAME.getValue()));
            userSqlResultSet.setBranchOpeningDate(resultSet.getDate(AppUserQueryParams.BRANCH_OPENING_DATE.getValue()));
            userSqlResultSet.setBranchStatus(resultSet.getString(AppUserQueryParams.BRANCH_STATUS.getValue()));
            userSqlResultSet.setRoleId(resultSet.getInt(AppUserQueryParams.ROLE_ID.getValue()));
            userSqlResultSet.setRoleName(resultSet.getString(AppUserQueryParams.ROLE_NAME.getValue()));
            userSqlResultSet.setRoleDescription(resultSet.getString(AppUserQueryParams.ROLE_DESCRIPTION.getValue()));
            userSqlResultSet.setRoleStatus(resultSet.getString(AppUserQueryParams.ROLE_STATUS.getValue()));
            userSqlResultSet.setPhoneNumber(resultSet.getString(AppUserQueryParams.PHONE_NUMBER.getValue()));
            return userSqlResultSet;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum AppUserQueryParams {
        LIMIT("limit"),
        OFFSET("offset"),
        ID("id"),
        ACCOUNT_ID("accountId"),
        ACCOUNT_TYPE("accountType"),
        FULL_NAME("fullName"),
        ADDRESS("address"),
        EMAIL("email"),
        USER_STATUS("userStatus"),
        DATE_OF_BIRTH("dateOfBirth"),
        ID_NUMBER("idNumber"),
        PHONE_NUMBER("phoneNumber"),
        COMPANY_NAME("companyName"),
        COMPANY_ID("companyId"),
        COMPANY_IDS("companyIds"),
        COMPANY_TYPE("companyType"),
        COMPANY_STATUS("companyStatus"),
        VERIFIED("verified"),
        BRANCH_ID("branchId"),
        BRANCH_IDS("branchIds"),
        BRANCH_NAME("branchName"),
        BRANCH_OPENING_DATE("branchOpeningDate"),
        BRANCH_STATUS("branchStatus"),
        ROLE_ID("roleId"),
        ROLE_NAME("roleName"),
        ROLE_DESCRIPTION("roleDescription"),
        ROLE_STATUS("roleStatus"),
        CREATED_BY_FULL_NAME("createdByFullName"),
        MODIFIED_BY_FULL_NAME("modifiedByFullName"),
        FIRST_NAME("firstName"),
        LAST_NAME("lastName");
        private final String value;
    }

    private final String BASE_USER_QUERY = """
            SELECT usr.id            AS id,
                   usr.first_name    AS firstName,
                   usr.last_name     AS lastName,
                   usr.address       AS address,
                   usr.email         AS email,
                   usr.record_status AS userStatus,
                   usr.date_of_birth AS dateOfBirth,
                   usr.id_number     AS idNumber,
                   usr.phone_number  AS phoneNumber,
                   usr.is_verified   AS verified,
                   com.id            AS companyId,
                   com.name          AS companyName,
                   com.company_type  AS companyType,
                   com.record_status AS companyStatus,
                   acc.id            AS accountId,
                   acc.country_code  AS countryCode,
                   acc.account_type  AS accountType,
                   br.id             AS branchId,
                   br.name           AS branchName,
                   br.created_on     AS branchOpeningDate,
                   br.record_status  AS branchStatus,
                   rl.id             AS roleId,
                   rl.name           AS roleName,
                   rl.description    AS roleDescription,
                   rl.record_status  AS roleStatus
            FROM app_user usr
                     INNER JOIN account acc
                                ON acc.id = usr.account_id
                     LEFT OUTER JOIN role rl
                                     ON rl.id = usr.role_id
                     LEFT OUTER JOIN company com
                                     ON com.id = usr.company_id
                     LEFT OUTER JOIN branch br
                                     ON br.id = usr.branch_id
                     LEFT OUTER JOIN app_user createdBy
                                     ON createdBy.id = usr.id
                     LEFT OUTER JOIN app_user modifiedBy
                                     ON modifiedBy.id = usr.id
            WHERE (usr.id = :id OR :id IS NULL)
              AND (CONCAT(usr.first_name, '', usr.last_name) LIKE
                   CONCAT('%', REPLACE(:fullName, ' ', ''), '%') OR
                   :fullName IS NULL)
              AND (usr.address LIKE CONCAT('%', :address, '%') OR :address IS NULL)
              AND (usr.email LIKE CONCAT('%', :email, '%') OR :email IS NULL)
              AND (usr.record_status = :userStatus OR :userStatus IS NULL)
              AND (usr.date_of_birth = DATE(:dateOfBirth) OR :dateOfBirth IS NULL)
              AND (usr.id_number LIKE CONCAT('%', :idNumber, '%') OR :idNumber IS NULL)
              AND (usr.phone_number LIKE CONCAT('%', :phoneNumber, '%') OR :phoneNumber IS NULL)
              AND (usr.is_verified = :verified OR :verified IS NULL)
              AND (CONCAT(createdBy.first_name, '', createdBy.last_name) LIKE
                   CONCAT('%', REPLACE(:createdByFullName, ' ', ''), '%') OR
                   :createdByFullName IS NULL)
              AND (CONCAT(modifiedBy.first_name, '', modifiedBy.last_name) LIKE
                   CONCAT('%', REPLACE(:modifiedByFullName, ' ', ''), '%') OR
                   :modifiedByFullName IS NULL)
              AND (usr.account_id = :accountId OR :accountId IS NULL)
              AND (com.name LIKE CONCAT('%', :companyName, '%') OR :companyName IS NULL)
              AND (br.name LIKE CONCAT('%', :branchName, '%') OR :branchName IS NULL)
              AND (usr.role_id = :roleId OR :roleId IS NULL)
              AND (rl.name LIKE CONCAT('%', :roleName, '%') OR :roleName IS NULL)
            """;

    public final String backofficeUserQuery() {
        return BASE_USER_QUERY + "" +
                "LIMIT :limit OFFSET :offset";
    }

    public final String companyUserQuery() {
        return BASE_USER_QUERY + "" +
                "   AND (usr.company_id IN (:companyIds))\n" +
                "   AND (usr.branch_id IN (:branchIds))\n" +
                "LIMIT :limit OFFSET :offset";

    }
}
