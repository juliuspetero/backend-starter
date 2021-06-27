package com.mojagap.backenstarter.dto.role;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class RoleDto {
    private Integer id;
    private String name;
    private String description;
    private String status;
    private boolean superUser;
    private List<PermissionDto> permissions;

    public RoleDto(Integer id, String name, String description, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public void isValid() {
        PowerValidator.validStringLength(name, 5, 100, ErrorMessages.INVALID_ROLE_NAME);
        PowerValidator.validStringLength(description, 10, 255, ErrorMessages.INVALID_ROLE_DESCRIPTION);
        PowerValidator.notEmpty(permissions, ErrorMessages.PERMISSIONS_REQUIRED_FOR_ROLE);
        PowerValidator.notNull(superUser, "Super User is a required property");
    }
}
