package com.mojagap.backenstarter.model.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojagap.backenstarter.dto.company.CompanyDto;
import com.mojagap.backenstarter.infrastructure.utility.DateUtil;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity(name = "company")
@Setter
@NoArgsConstructor
public class Company extends AuditEntity {
    private String name;
    @JsonFormat(pattern = DateUtil.DD_MMM_YYY)
    private Date registrationDate;
    private CompanyType companyType;
    private String registrationNumber;
    private String address;
    private String email;
    private String phoneNumber;
    private Account account;
    private Company parentCompany;
    private Set<Branch> branches = new HashSet<>();

    public Company(CompanyDto companyDto) {
        BeanUtils.copyProperties(companyDto, this);
    }

    @Column(name = "name")
    @Size(min = 6, max = 100)
    public String getName() {
        return name;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "registration_date")
    public Date getRegistrationDate() {
        return registrationDate;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type")
    public CompanyType getCompanyType() {
        return companyType;
    }

    @Column(name = "registration_number")
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    @Column(name = "address")
    public String getAddress() {
        return address;
    }

    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    @Column(name = "phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    public Account getAccount() {
        return account;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_company_id")
    public Company getParentCompany() {
        return parentCompany;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company", fetch = FetchType.LAZY)
    public Set<Branch> getBranches() {
        return branches;
    }
}
