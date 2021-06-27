package com.mojagap.backenstarter.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.dto.branch.BranchDto;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.user.IdentificationEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class AppUserDto {
    private Integer id;
    private String authentication;
    private String firstName;
    private String lastName;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date dateOfBirth;
    private String idNumber;
    private String idType;
    private String address;
    private String email;
    private String phoneNumber;
    private String status;
    private String password;
    private Boolean verified = Boolean.FALSE;
    private CompanyDto company;
    private AccountDto account;
    private BranchDto branch;
    private RoleDto role;
    private AppUserDto createdBy;
    private AppUserDto modifiedBy;

    public AppUserDto(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @SneakyThrows
    public void isValid() {
        PowerValidator.validStringLength(firstName, 5, 255, ErrorMessages.INVALID_FIRST_NAME);
        PowerValidator.validStringLength(lastName, 5, 255, ErrorMessages.INVALID_LAST_NAME);
        PowerValidator.validStringLength(idNumber, 5, 255, ErrorMessages.INVALID_ID_NUMBER_PROVIDED);
        PowerValidator.ValidEnum(IdentificationEnum.class, idType, ErrorMessages.INVALID_ID_TYPE);
        PowerValidator.notNull(dateOfBirth, ErrorMessages.DATE_OF_BIRTH_REQUIRED);
        PowerValidator.validStringLength(address, 10, 255, ErrorMessages.INVALID_LOCATION_ADDRESS);
        PowerValidator.validEmail(email, ErrorMessages.INVALID_EMAIL_ADDRESS);
        PowerValidator.validPhoneNumber(phoneNumber, ErrorMessages.INVALID_PHONE_NUMBER);
    }
}
