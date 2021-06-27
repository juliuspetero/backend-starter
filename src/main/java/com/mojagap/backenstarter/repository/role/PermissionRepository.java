package com.mojagap.backenstarter.repository.role;


import com.mojagap.backenstarter.model.role.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Permission findOneByName(String name);
}
