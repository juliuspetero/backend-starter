package com.mojagap.backenstarter.model.branch;

import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.company.Company;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "branch")
@Setter
@NoArgsConstructor
public class Branch extends AuditEntity {
    private String name;
    private Branch parentBranch;
    private Company company;

    public Branch(String name, Company company) {
        this.name = name;
        this.company = company;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_branch_id")
    public Branch getParentBranch() {
        return parentBranch;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    public Company getCompany() {
        return company;
    }
}
