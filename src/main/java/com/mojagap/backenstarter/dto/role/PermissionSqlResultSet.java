package com.mojagap.backenstarter.dto.role;

import lombok.Data;

@Data
public class PermissionSqlResultSet {
    private Integer id;
    private String name;
    private String actionType;
    private String entityType;
    private String category;
}
