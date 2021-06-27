package com.mojagap.backenstarter.model.common;

import com.mojagap.backenstarter.model.user.AppUser;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@MappedSuperclass
public class AuditEntity extends BaseEntity {
    private Date createdOn;
    private Date modifiedOn;
    private AppUser createdBy;
    private AppUser modifiedBy;
    private RecordStatus recordStatus = RecordStatus.ACTIVE;

    @Column(name = "created_on")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreatedOn() {
        return createdOn;
    }

    @Column(name = "modified_on")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModifiedOn() {
        return modifiedOn;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    public AppUser getCreatedBy() {
        return createdBy;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modified_by")
    public AppUser getModifiedBy() {
        return modifiedBy;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "record_status")
    public RecordStatus getRecordStatus() {
        return recordStatus;
    }

    public enum RecordStatus {
        ACTIVE,
        REJECTED,
        APPROVED,
        INACTIVE,
        DELETED,
        CLOSED;
    }
}
