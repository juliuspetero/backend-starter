package com.mojagap.backenstarter.model.user;

import com.mojagap.backenstarter.dto.user.AppUserDto;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.branch.Branch;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import com.mojagap.backenstarter.model.role.Role;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.util.Date;

@Setter
@Entity(name = "app_user")
@NoArgsConstructor
public class AppUser extends AuditEntity {
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String idNumber;
    private String address;
    private String email;
    private String phoneNumber;
    private String password;
    private Boolean verified = Boolean.FALSE;
    private Account account;
    private Company company;
    private Branch branch;
    private Role role;

    public AppUser(AppUserDto appUserDto) {
        BeanUtils.copyProperties(appUserDto, this);
    }

    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Column(name = "id_number")
    public String getIdNumber() {
        return idNumber;
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

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    @Column(name = "is_verified")
    public Boolean getVerified() {
        return verified;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    public Account getAccount() {
        return account;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "company_id")
    public Company getCompany() {
        return company;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "branch_id")
    public Branch getBranch() {
        return branch;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id")
    public Role getRole() {
        return role;
    }
}
