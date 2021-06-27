package com.mojagap.backenstarter.service.company;


import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.company.CompanySqlResultSet;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.utility.CommonUtil;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.service.company.handler.CompanyQueryHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyQueryService implements CompanyQueryHandler {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ModelMapper modelMapper;

    @Autowired
    public CompanyQueryService(NamedParameterJdbcTemplate jdbcTemplate, ModelMapper modelMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.modelMapper = modelMapper;
    }

    @Override
    public RecordHolder<CompanyDto> getCompanies(Map<String, String> queryParams) {
        Arrays.asList(CompanyQueryParams.values()).forEach(param -> queryParams.putIfAbsent(param.getValue(), null));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(queryParams);
        List<Integer> branches = AppContext.getCompaniesOfLoggedInUser().stream().map(Company::getId).collect(Collectors.toList());
        mapSqlParameterSource.addValue(CompanyQueryParams.LOGGED_IN_USER_COMPANY_IDS.getValue(), branches);
        Integer limit = queryParams.get(CompanyQueryParams.LIMIT.getValue()) != null ? Integer.parseInt(queryParams.get(CompanyQueryParams.LIMIT.getValue())) : Integer.MAX_VALUE;
        mapSqlParameterSource.addValue(CompanyQueryParams.LIMIT.getValue(), limit, Types.INTEGER);
        Integer offset = queryParams.get(CompanyQueryParams.OFFSET.getValue()) != null ? Integer.parseInt(queryParams.get(CompanyQueryParams.OFFSET.getValue())) : 0;
        mapSqlParameterSource.addValue(CompanyQueryParams.OFFSET.getValue(), offset, Types.INTEGER);
        List<CompanySqlResultSet> sqlResultSets = jdbcTemplate.query(companyQuery(), mapSqlParameterSource, new CompanyMapper());
        List<CompanyDto> companyDtos = sqlResultSets.stream().map(this::toCompanyDto).collect(Collectors.toList());
        return new RecordHolder<>(companyDtos.size(), companyDtos);
    }

    private CompanyDto toCompanyDto(CompanySqlResultSet companySqlResultSet) {
        CompanyDto companyDto = CommonUtil.copyProperties(companySqlResultSet, new CompanyDto());
        companyDto.setParentCompany(new CompanyDto(companySqlResultSet.getParentCompanyId(), companySqlResultSet.getParentCompanyName(), null, companySqlResultSet.getParentCompanyStatus()));
        companyDto.setCreatedByUser(new AppUserDto(companySqlResultSet.getCreatedById(), companySqlResultSet.getCreatedByFirstName(), companySqlResultSet.getCreatedByLastName()));
        companyDto.setAccount(new AccountDto(companySqlResultSet.getAccountId()));
        return companyDto;
    }

    private static final class CompanyMapper implements RowMapper<CompanySqlResultSet> {

        @Override
        public CompanySqlResultSet mapRow(ResultSet resultSet, int i) throws SQLException {
            CompanySqlResultSet companySqlResultSet = new CompanySqlResultSet();
            companySqlResultSet.setId(resultSet.getInt(CompanyQueryParams.ID.getValue()));
            companySqlResultSet.setName(resultSet.getString(CompanyQueryParams.NAME.getValue()));
            companySqlResultSet.setStatus(resultSet.getString(CompanyQueryParams.STATUS.getValue()));
            companySqlResultSet.setCompanyType(resultSet.getString(CompanyQueryParams.COMPANY_TYPE.getValue()));
            companySqlResultSet.setEmail(resultSet.getString(CompanyQueryParams.EMAIL.getValue()));
            companySqlResultSet.setPhoneNumber(resultSet.getString(CompanyQueryParams.PHONE_NUMBER.getValue()));
            companySqlResultSet.setAddress(resultSet.getString(CompanyQueryParams.ADDRESS.getValue()));
            companySqlResultSet.setOpeningDate(resultSet.getDate(CompanyQueryParams.OPENING_DATE.getValue()));
            companySqlResultSet.setRegistrationDate(resultSet.getDate(CompanyQueryParams.REGISTRATION_DATE.getValue()));
            companySqlResultSet.setRegistrationNumber(resultSet.getString(CompanyQueryParams.REGISTRATION_NUMBER.getValue()));
            companySqlResultSet.setAccountId(resultSet.getInt(CompanyQueryParams.ACCOUNT_ID.getValue()));
            companySqlResultSet.setParentCompanyId(resultSet.getInt(CompanyQueryParams.PARENT_COMPANY_ID.getValue()));
            companySqlResultSet.setParentCompanyName(resultSet.getString(CompanyQueryParams.PARENT_COMPANY_NAME.getValue()));
            companySqlResultSet.setParentCompanyStatus(resultSet.getString(CompanyQueryParams.PARENT_COMPANY_STATUS.getValue()));
            companySqlResultSet.setCreatedById(resultSet.getInt(CompanyQueryParams.CREATED_BY_ID.getValue()));
            companySqlResultSet.setCreatedByFirstName(resultSet.getString(CompanyQueryParams.CREATED_BY_FIRST_NAME.getValue()));
            companySqlResultSet.setCreatedByLastName(resultSet.getString(CompanyQueryParams.CREATED_BY_LAST_NAME.getValue()));
            return companySqlResultSet;
        }
    }


    private String companyQuery() {
        return """
                SELECT com.id                  AS id,
                       com.name                AS name,
                       com.record_status       AS status,
                       com.company_type        AS companyType,
                       com.email               AS email,
                       com.phone_number        AS phoneNumber,
                       com.registration_date AS registrationDate,
                       com.registration_number   AS registrationNumber,
                       com.address             AS address,
                       com.account_id          AS accountId,
                       com.created_on          AS openingDate,
                       parent.id               AS parentCompanyId,
                       parent.name             AS parentCompanyName,
                       parent.record_status    AS parentCompanyStatus,
                       parent.company_type     AS parentCompanyType,
                       createdBy.id            AS createdById,
                       createdBy.first_name    AS createdByFirstName,
                       createdBy.last_name     AS createdByLastName
                FROM company com
                         INNER JOIN company parent
                                    ON parent.id = com.parent_company_id
                         INNER JOIN app_user createdBy
                                    ON createdBy.id = com.created_by
                WHERE (com.id = :id OR :id IS NULL)
                  AND (com.name LIKE CONCAT('%', :name, '%') OR :name IS NULL)
                  AND (com.record_status LIKE CONCAT('%', :status, '%') OR :status IS NULL)
                  AND (com.company_type LIKE CONCAT('%', :companyType, '%') OR :companyType IS NULL)
                  AND (com.parent_company_id = :parentCompanyId OR :parentCompanyId IS NULL)
                  AND (com.registration_date = DATE(:registrationDate) OR :registrationDate IS NULL)
                  AND (com.registration_number LIKE CONCAT('%', :registrationNumber, '%') OR :registrationNumber IS NULL)
                  AND (com.account_id = :accountId OR :accountId IS NULL)
                  AND (com.email LIKE CONCAT('%', :email, '%') OR :email IS NULL)
                  AND (com.phone_number LIKE CONCAT('%', :phoneNumber, '%') OR :phoneNumber IS NULL)
                  AND (com.address LIKE CONCAT('%', :address, '%') OR :address IS NULL)
                  AND (com.parent_company_id = :parentCompanyId OR :parentCompanyId IS NULL)
                  AND (com.id IN (:companyIds))
                """;
    }

    @AllArgsConstructor
    @Getter
    public enum CompanyQueryParams {
        LIMIT("limit"),
        OFFSET("offset"),
        ID("id"),
        NAME("name"),
        OPENING_DATE("openingDate"),
        STATUS("status"),
        COMPANY_TYPE("companyType"),
        EMAIL("email"),
        PHONE_NUMBER("phoneNumber"),
        REGISTRATION_DATE("registrationDate"),
        REGISTRATION_NUMBER("registrationNumber"),
        PARENT_COMPANY_ID("parentCompanyId"),
        PARENT_COMPANY_NAME("parentCompanyName"),
        PARENT_COMPANY_STATUS("parentCompanyStatus"),
        ADDRESS("address"),
        ACCOUNT_ID("accountId"),
        CREATED_BY_ID("createdById"),
        CREATED_BY_FIRST_NAME("createdByFirstName"),
        CREATED_BY_LAST_NAME("createdByLastName"),
        LOGGED_IN_USER_COMPANY_IDS("companyIds");
        private final String value;
    }


}
