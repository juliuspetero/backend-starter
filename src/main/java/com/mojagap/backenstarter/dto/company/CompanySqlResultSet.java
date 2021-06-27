package com.mojagap.backenstarter.dto.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class CompanySqlResultSet {
    private Integer id;
    private String name;
    private String status;
    private String companyType;
    private String email;
    private String phoneNumber;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date registrationDate;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date openingDate;
    private String registrationNumber;
    private String address;
    private Integer accountId;
    private Integer parentCompanyId;
    private String parentCompanyName;
    private String parentCompanyStatus;
    private Integer createdById;
    private String createdByFirstName;
    private String createdByLastName;
}
