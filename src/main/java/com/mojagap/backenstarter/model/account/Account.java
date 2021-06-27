package com.mojagap.backenstarter.model.account;


import com.mojagap.backenstarter.dto.account.AccountDto;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.user.AppUser;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Setter
@Entity(name = "account")
@NoArgsConstructor
public class Account extends AuditEntity {
    private String name;
    private String address;
    private CountryCode countryCode;
    private String email;
    private String contactPhoneNumber;
    private Date activatedOn;
    private AppUser activatedBy;
    private AccountType accountType;
    private Set<AppUser> appUsers = new HashSet<>();
    private Set<Company> companies = new HashSet<>();

    public Account(AccountDto accountDto) {
        this.countryCode = CountryCode.getByCode(accountDto.getCountryCode());
        this.accountType = AccountType.valueOf(accountDto.getAccountType());
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "country_code")
    @NotNull(message = "Please provide a valid country code")
    public CountryCode getCountryCode() {
        return countryCode;
    }

    @NotBlank(message = "Contact Phone Number is required")
    @Column(name = "contact_phone_number")
    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    @Column(name = "email")
    @Email(message = "Please provide a valid email address")
    public String getEmail() {
        return email;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "activated_by")
    public AppUser getActivatedBy() {
        return activatedBy;
    }

    @Column(name = "activated_on")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getActivatedOn() {
        return activatedOn;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    @NotNull(message = "Account Type cannot be empty")
    public AccountType getAccountType() {
        return accountType;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", fetch = FetchType.LAZY)
    public Set<AppUser> getAppUsers() {
        return appUsers;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account", fetch = FetchType.LAZY)
    public Set<Company> getCompanies() {
        return companies;
    }
}

