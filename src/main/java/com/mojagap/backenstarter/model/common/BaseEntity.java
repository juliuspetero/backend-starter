package com.mojagap.backenstarter.model.common;

import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


@MappedSuperclass
@Setter
public class BaseEntity implements Serializable {
    private Integer id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer getId() {
        return id;
    }
}
