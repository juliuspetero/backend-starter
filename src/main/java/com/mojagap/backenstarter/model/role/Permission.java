package com.mojagap.backenstarter.model.role;

import com.mojagap.backenstarter.model.common.ActionTypeEnum;
import com.mojagap.backenstarter.model.common.BaseEntity;
import com.mojagap.backenstarter.model.common.EntityTypeEnum;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Setter
@Entity(name = "permission")
@NoArgsConstructor
public class Permission extends BaseEntity {
    private String name;
    private EntityTypeEnum entityType;
    private ActionTypeEnum actionType;
    private PermCategoryEnum category;

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    public EntityTypeEnum getEntityType() {
        return entityType;
    }

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    public ActionTypeEnum getActionType() {
        return actionType;
    }

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    public PermCategoryEnum getCategory() {
        return category;
    }
}
