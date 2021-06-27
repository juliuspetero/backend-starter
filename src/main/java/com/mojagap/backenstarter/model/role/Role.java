package com.mojagap.backenstarter.model.role;


import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Entity(name = "role")
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {
    private String name;
    private String description;
    private AuditEntity.RecordStatus status = AuditEntity.RecordStatus.ACTIVE;
    private Account account;
    private List<Permission> permissions;

    public Role(String name, String description, Account account, List<Permission> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.account = account;
    }

    @Column(name = "name")
    @NotBlank(message = "Role name is required")
    public String getName() {
        return name;
    }

    @Column(name = "description")
    @NotBlank(message = "Role description is needed")
    public String getDescription() {
        return description;
    }

    @Column(name = "record_status")
    @Enumerated(EnumType.STRING)
    public AuditEntity.RecordStatus getStatus() {
        return status;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    public Account getAccount() {
        return account;
    }

    @JoinTable(name = "role_permission", joinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "permission_id", referencedColumnName = "id")})
    @OneToMany(targetEntity = Permission.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public List<Permission> getPermissions() {
        return permissions;
    }

    @Transient
    public Boolean isSuperUser() {
        return permissions != null && permissions.stream().map(Permission::getName).collect(Collectors.toList()).contains(PermissionEnum.SUPER_PERMISSION.name());
    }
}
