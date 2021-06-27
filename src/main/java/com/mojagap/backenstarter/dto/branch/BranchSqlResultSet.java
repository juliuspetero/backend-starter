package com.mojagap.backenstarter.dto.branch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class BranchSqlResultSet {
    private Integer id;
    private String name;
    private String status;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date openingDate;
    private Integer companyId;
    private String companyName;
    private String companyStatus;
    private Integer parentBranchId;
    private String parentBranchName;
    private String parentBranchStatus;
    private Integer createdById;
    private String createdByFirstName;
    private String createdByLastName;
}
