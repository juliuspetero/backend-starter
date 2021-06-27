package com.mojagap.backenstarter.service.role;

import com.mojagap.backenstarter.dto.role.PermissionDto;
import com.mojagap.backenstarter.dto.role.PermissionSqlResultSet;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.model.common.RecordHolder;
import com.mojagap.backenstarter.model.role.Role;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.role.RoleRepository;
import com.mojagap.backenstarter.service.role.handler.RoleQueryHandler;
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
public class RoleQueryService implements RoleQueryHandler {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;

    @Autowired
    public RoleQueryService(NamedParameterJdbcTemplate jdbcTemplate, ModelMapper modelMapper, RoleRepository roleRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
    }


    @Override
    public RecordHolder<RoleDto> getRoles(Map<String, String> queryParams) {
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Integer accountId = loggedInUser.getAccount().getId();
        Integer id = queryParams.get(RoleQueryParams.ID.getValue()) != null ?
                Integer.parseInt(queryParams.get(RoleQueryParams.ID.getValue())) : null;
        String name = queryParams.get(RoleQueryParams.NAME.getValue());
        String description = queryParams.get(RoleQueryParams.DESCRIPTION.getValue());
        List<Role> roles = roleRepository.findByQueryParams(id, accountId, name, description);
        List<RoleDto> roleDtos = roles.stream().map(role -> {
            RoleDto roleDto = modelMapper.map(role, RoleDto.class);
            roleDto.setSuperUser(role.isSuperUser());
            return roleDto;
        }).collect(Collectors.toList());
        return new RecordHolder<>(roleDtos.size(), roleDtos);
    }

    private static final class PermissionMapper implements RowMapper<PermissionSqlResultSet> {
        @Override
        public PermissionSqlResultSet mapRow(ResultSet resultSet, int i) throws SQLException {
            PermissionSqlResultSet permissionSqlResultSet = new PermissionSqlResultSet();
            permissionSqlResultSet.setId(resultSet.getInt(PermissionQueryParams.ID.getValue()));
            permissionSqlResultSet.setName(resultSet.getString(PermissionQueryParams.NAME.getValue()));
            permissionSqlResultSet.setActionType(resultSet.getString(PermissionQueryParams.ACTION_TYPE.getValue()));
            permissionSqlResultSet.setEntityType(resultSet.getString(PermissionQueryParams.ENTITY_TYPE.getValue()));
            permissionSqlResultSet.setCategory(resultSet.getString(PermissionQueryParams.CATEGORY.getValue()));
            return permissionSqlResultSet;
        }
    }

    @Override
    public RecordHolder<PermissionDto> getPermissions(Map<String, String> queryParams) {
        AppUser loggedInUser = AppContext.getLoggedInUser();
        Arrays.asList(PermissionQueryParams.values()).forEach(param -> queryParams.putIfAbsent(param.getValue(), null));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(queryParams);
        mapSqlParameterSource.addValue(PermissionQueryParams.CATEGORY.getValue(), loggedInUser.getAccount().getAccountType().name());
        Integer limit = queryParams.get(PermissionQueryParams.LIMIT.getValue()) != null ? Integer.parseInt(queryParams.get(PermissionQueryParams.LIMIT.getValue())) : Integer.MAX_VALUE;
        mapSqlParameterSource.addValue(PermissionQueryParams.LIMIT.getValue(), limit, Types.INTEGER);
        Integer offset = queryParams.get(PermissionQueryParams.OFFSET.getValue()) != null ? Integer.parseInt(queryParams.get(PermissionQueryParams.OFFSET.getValue())) : 0;
        mapSqlParameterSource.addValue(PermissionQueryParams.OFFSET.getValue(), offset, Types.INTEGER);
        List<PermissionSqlResultSet> sqlResultSets = jdbcTemplate.query(permissionQuery(), mapSqlParameterSource, new PermissionMapper());
        List<PermissionDto> permissionDtos = sqlResultSets.stream().map(resultSet -> modelMapper.map(resultSet, PermissionDto.class))
                .collect(Collectors.toList());
        return new RecordHolder<>(permissionDtos.size(), permissionDtos);
    }

    private String permissionQuery() {
        return """
                SELECT pm.id          AS id,
                       pm.name        AS name,
                       pm.action_type AS actionType,
                       pm.entity_type AS entityType,
                       pm.category    AS category
                FROM permission pm
                WHERE (pm.category LIKE CONCAT('%', :category, '%')
                    OR pm.category = 'GENERAL')
                  AND (pm.id = :id OR :id IS NULL)
                  AND (pm.name LIKE CONCAT('%', :name, '%') OR :name IS NULL)
                  AND (pm.action_type LIKE CONCAT('%', :actionType, '%') OR :actionType IS NULL)
                  AND (pm.entity_type LIKE CONCAT('%', :entityType, '%') OR :entityType IS NULL)
                LIMIT :limit OFFSET :offset
                """;
    }

    @AllArgsConstructor
    @Getter
    public enum PermissionQueryParams {
        LIMIT("limit"),
        OFFSET("offset"),
        ID("id"),
        CATEGORY("category"),
        NAME("name"),
        ENTITY_TYPE("entityType"),
        ACTION_TYPE("actionType");
        private final String value;
    }

    @AllArgsConstructor
    @Getter
    public enum RoleQueryParams {
        LIMIT("limit"),
        OFFSET("offset"),
        ID("id"),
        ACCOUNT_ID("category"),
        NAME("name"),
        DESCRIPTION("entityType");
        private final String value;
    }
}
