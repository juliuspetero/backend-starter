package com.mojagap.backenstarter.service.branch;

import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.dto.branch.BranchSqlResultSet;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.utility.CommonUtil;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.service.branch.handler.BranchQueryHandler;
import com.mojagap.backenstarter.service.role.RoleQueryService;
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
public class BranchQueryService implements BranchQueryHandler {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ModelMapper modelMapper;

    @Autowired
    public BranchQueryService(NamedParameterJdbcTemplate jdbcTemplate, ModelMapper modelMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.modelMapper = modelMapper;
    }

    @Override
    public RecordHolder<BranchDto> getBranches(Map<String, String> queryParams) {
        Arrays.asList(BranchQueryParams.values()).forEach(param -> queryParams.putIfAbsent(param.getValue(), null));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(queryParams);
        List<Integer> branches = AppContext.getBranchesOfLoggedInUser().stream().map(Branch::getId).collect(Collectors.toList());
        mapSqlParameterSource.addValue(BranchQueryParams.LOGGED_IN_USER_BRANCH_IDS.getValue(), branches);
        Integer limit = queryParams.get(BranchQueryParams.LIMIT.getValue()) != null ? Integer.parseInt(queryParams.get(RoleQueryService.PermissionQueryParams.LIMIT.getValue())) : Integer.MAX_VALUE;
        mapSqlParameterSource.addValue(BranchQueryParams.LIMIT.getValue(), limit, Types.INTEGER);
        Integer offset = queryParams.get(BranchQueryParams.OFFSET.getValue()) != null ? Integer.parseInt(queryParams.get(RoleQueryService.PermissionQueryParams.OFFSET.getValue())) : 0;
        mapSqlParameterSource.addValue(BranchQueryParams.OFFSET.getValue(), offset, Types.INTEGER);
        List<BranchSqlResultSet> sqlResultSets = jdbcTemplate.query(branchQuery(), mapSqlParameterSource, new BranchMapper());
        List<BranchDto> branchDtos = sqlResultSets.stream().map(this::toBranchDto)
                .collect(Collectors.toList());
        return new RecordHolder<>(branchDtos.size(), branchDtos);
    }

    private BranchDto toBranchDto(BranchSqlResultSet branchSqlResultSet) {
        BranchDto branchDto = CommonUtil.copyProperties(branchSqlResultSet, new BranchDto());
        branchDto.setParentBranch(new BranchDto(branchSqlResultSet.getParentBranchId(), branchSqlResultSet.getParentBranchName(), null, branchSqlResultSet.getParentBranchStatus()));
        branchDto.setCompany(new CompanyDto(branchSqlResultSet.getCompanyId(), branchSqlResultSet.getCompanyName(), null, branchSqlResultSet.getCompanyStatus()));
        branchDto.setCreatedBy(new AppUserDto(branchSqlResultSet.getCreatedById(), branchSqlResultSet.getCreatedByFirstName(), branchSqlResultSet.getCreatedByLastName()));
        return branchDto;
    }

    private static final class BranchMapper implements RowMapper<BranchSqlResultSet> {

        @Override
        public BranchSqlResultSet mapRow(ResultSet resultSet, int i) throws SQLException {
            BranchSqlResultSet branchSqlResultSet = new BranchSqlResultSet();
            branchSqlResultSet.setId(resultSet.getInt(BranchQueryParams.ID.getValue()));
            branchSqlResultSet.setName(resultSet.getString(BranchQueryParams.NAME.getValue()));
            branchSqlResultSet.setStatus(resultSet.getString(BranchQueryParams.STATUS.getValue()));
            branchSqlResultSet.setOpeningDate(resultSet.getDate(BranchQueryParams.OPENING_DATE.getValue()));
            branchSqlResultSet.setCompanyId(resultSet.getInt(BranchQueryParams.COMPANY_ID.getValue()));
            branchSqlResultSet.setCompanyName(resultSet.getString(BranchQueryParams.COMPANY_NAME.getValue()));
            branchSqlResultSet.setCompanyStatus(resultSet.getString(BranchQueryParams.COMPANY_STATUS.getValue()));
            branchSqlResultSet.setParentBranchId(resultSet.getInt(BranchQueryParams.PARENT_BRANCH_ID.getValue()));
            branchSqlResultSet.setParentBranchName(resultSet.getString(BranchQueryParams.PARENT_BRANCH_NAME.getValue()));
            branchSqlResultSet.setParentBranchStatus(resultSet.getString(BranchQueryParams.PARENT_BRANCH_STATUS.getValue()));
            branchSqlResultSet.setCreatedById(resultSet.getInt(BranchQueryParams.CREATED_BY_ID.getValue()));
            branchSqlResultSet.setCreatedByFirstName(resultSet.getString(BranchQueryParams.CREATED_BY_FIRST_NAME.getValue()));
            branchSqlResultSet.setCreatedByLastName(resultSet.getString(BranchQueryParams.CREATED_BY_LAST_NAME.getValue()));
            return branchSqlResultSet;
        }
    }


    private String branchQuery() {
        return """
                SELECT br.id                AS id,
                       br.name              AS name,
                       br.record_status     AS Status,
                       br.created_on        AS openingDate,
                       com.id               AS companyId,
                       com.name             AS companyName,
                       com.record_status    AS companyStatus,
                       parent.id            AS parentBranchId,
                       parent.name          AS parentBranchName,
                       parent.record_status AS parentBranchStatus,
                       createdBy.id         AS createdById,
                       createdBy.first_name AS createdByFirstName,
                       createdBy.last_name  AS createdByLastName
                FROM branch br
                         INNER JOIN company com
                                    ON com.id = br.company_id
                         INNER JOIN branch parent
                                    ON parent.id = br.parent_branch_id
                         INNER JOIN app_user createdBy
                                    ON createdBy.id = br.created_by
                WHERE (br.id = :id OR :id IS NULL)
                  AND (br.name LIKE CONCAT('%', :name, '%') OR :name IS NULL)
                  AND (br.company_id = :companyId OR :companyId IS NULL)
                  AND (br.parent_branch_id = :parentBranchId OR :parentBranchId IS NULL)
                  AND (br.id IN (:branchIds))
                """;
    }

    @AllArgsConstructor
    @Getter
    public enum BranchQueryParams {
        LIMIT("limit"),
        OFFSET("offset"),
        ID("id"),
        NAME("name"),
        OPENING_DATE("openingDate"),
        STATUS("Status"),
        COMPANY_ID("companyId"),
        COMPANY_NAME("companyName"),
        COMPANY_STATUS("companyStatus"),
        PARENT_BRANCH_ID("parentBranchId"),
        PARENT_BRANCH_NAME("parentBranchName"),
        PARENT_BRANCH_STATUS("parentBranchId"),
        CREATED_BY_ID("createdById"),
        CREATED_BY_FIRST_NAME("createdByFirstName"),
        CREATED_BY_LAST_NAME("createdByLastName"),
        LOGGED_IN_USER_BRANCH_IDS("branchIds");
        private final String value;
    }
}
