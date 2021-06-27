package com.mojagap.backenstarter.dto.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.i18n.phonenumbers.NumberParseException;
import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.company.CompanyType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class CompanyDto {
    private Integer id;
    private String name;
    private String companyType;
    private String status;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date registrationDate;
    private String registrationNumber;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date openingDate;
    private String address;
    private String email;
    private String phoneNumber;
    private AccountDto account;
    private CompanyDto parentCompany;
    private AppUserDto createdByUser;
    private List<AppUserDto> appUsers;

    public CompanyDto(Integer id) {
        this.id = id;
    }

    public CompanyDto(Integer id, String name, String companyType, String status) {
        this.id = id;
        this.name = name;
        this.companyType = companyType;
        this.status = status;
    }

    @SneakyThrows(NumberParseException.class)
    public void isValid() {
        PowerValidator.ValidEnum(CompanyType.class, companyType, ErrorMessages.VALID_COMPANY_TYPE);
        PowerValidator.validStringLength(name, 5, 255, ErrorMessages.INVALID_COMPANY_NAME);
        PowerValidator.notNull(registrationDate, ErrorMessages.COMPANY_REGISTRATION_DATE_REQUIRED);
        PowerValidator.validStringLength(address, 10, 255, ErrorMessages.INVALID_LOCATION_ADDRESS);
        PowerValidator.validEmail(email, ErrorMessages.INVALID_EMAIL_ADDRESS);
        PowerValidator.validPhoneNumber(phoneNumber, ErrorMessages.INVALID_PHONE_NUMBER);
    }

}
