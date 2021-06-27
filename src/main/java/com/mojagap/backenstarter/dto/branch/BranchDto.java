package com.mojagap.backenstarter.dto.branch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class BranchDto {
    private Integer id;
    private String name;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date openingDate;
    private String status;
    private BranchDto parentBranch;
    private CompanyDto company;
    private AppUserDto createdBy;

    public BranchDto(Integer id, String name, Date openingDate, String status) {
        this.id = id;
        this.name = name;
        this.openingDate = openingDate;
        this.status = status;
    }

    public void isValid() {
        PowerValidator.validStringLength(name, 5, 255, ErrorMessages.INVALID_BRANCH_NAME);
        if (parentBranch == null || parentBranch.id == null) {
            PowerValidator.throwBadRequestException(String.format(ErrorMessages.ENTITY_REQUIRED, "Parent branch"));
        }
    }
}
