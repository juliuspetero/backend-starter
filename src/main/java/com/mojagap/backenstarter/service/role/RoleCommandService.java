package com.mojagap.backenstarter.service.role;

import com.mojagap.backenstarter.dto.ActionResponse;
import com.mojagap.backenstarter.dto.role.PermissionDto;
import com.mojagap.backenstarter.dto.role.RoleDto;
import com.mojagap.backenstarter.infrastructure.AppContext;
import com.mojagap.backenstarter.infrastructure.ErrorMessages;
import com.mojagap.backenstarter.infrastructure.PowerValidator;
import com.mojagap.backenstarter.infrastructure.exception.BadRequestException;
import com.mojagap.backenstarter.model.account.Account;
import com.mojagap.backenstarter.model.account.AccountType;
import com.mojagap.backenstarter.model.common.AuditEntity;
import com.mojagap.backenstarter.model.role.PermCategoryEnum;
import com.mojagap.backenstarter.model.role.Permission;
import com.mojagap.backenstarter.model.role.PermissionEnum;
import com.mojagap.backenstarter.model.role.Role;
import com.mojagap.backenstarter.model.user.AppUser;
import com.mojagap.backenstarter.repository.role.PermissionRepository;
import com.mojagap.backenstarter.repository.role.RoleRepository;
import com.mojagap.backenstarter.repository.user.AppUserRepository;
import com.mojagap.backenstarter.service.role.handler.RoleCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RoleCommandService implements RoleCommandHandler {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;


    @Autowired
    public RoleCommandService(PermissionRepository permissionRepository, RoleRepository roleRepository, AppUserRepository appUserRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
    }


    @Override
    @Transactional
    public ActionResponse createRole(RoleDto roleDto) {
        roleDto.isValid();
        Account account = AppContext.getLoggedInUser().getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY, AccountType.BACK_OFFICE);
        List<Role> existingRoles = roleRepository.findByAccountIdAndName(roleDto.getName(), account.getId());
        PowerValidator.isEmpty(existingRoles, String.format(ErrorMessages.ENTITY_ALREADY_EXISTS, Role.class.getSimpleName(), "name"));
        List<Permission> permissions = validatePermissions(roleDto, account);
        Role role = new Role(roleDto.getName(), roleDto.getDescription(), account, permissions);
        role = roleRepository.save(role);
        return new ActionResponse(role.getId());
    }

    @Override
    @Transactional
    public ActionResponse updateRole(RoleDto roleDto, Integer roleId) {
        roleDto.isValid();
        Account account = AppContext.getLoggedInUser().getAccount();
        PowerValidator.iPermittedAccountType(account.getAccountType(), AccountType.COMPANY, AccountType.BACK_OFFICE);
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Role.class.getSimpleName(), "id")));
        if (!roleDto.getName().equals(role.getName())) {
            List<Role> existingRoles = roleRepository.findByAccountIdAndName(roleDto.getName(), account.getId());
            PowerValidator.isEmpty(existingRoles, String.format(ErrorMessages.ENTITY_ALREADY_EXISTS, Role.class.getSimpleName(), "name"));
        }
        List<Permission> permissions = validatePermissions(roleDto, account);
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setPermissions(permissions);
        roleRepository.save(role);
        return new ActionResponse(roleId);
    }

    @Override
    public ActionResponse removeRole(Integer roleId) {
        Account account = AppContext.getLoggedInUser().getAccount();
        Role role = roleRepository.findByIdAndAccountId(roleId, account.getId()).orElseThrow(() -> new BadRequestException(String.format(ErrorMessages.ENTITY_DOES_NOT_EXISTS, Role.class.getSimpleName(), "id")));
        List<AppUser> users = appUserRepository.findByRoleId(roleId);
        PowerValidator.isEmpty(users, ErrorMessages.ROLE_IS_IN_USE);
        role.setStatus(AuditEntity.RecordStatus.DELETED);
        roleRepository.save(role);
        return new ActionResponse(roleId);
    }


    private List<Permission> validatePermissions(RoleDto roleDto, Account account) {
        List<Permission> permissions = new ArrayList<>();
        if (roleDto.isSuperUser()) {
            permissions.add(permissionRepository.findOneByName(PermissionEnum.SUPER_PERMISSION.name()));
        } else {
            List<Integer> permissionIds = roleDto.getPermissions().stream().map(PermissionDto::getId).collect(Collectors.toList());
            permissions = permissionRepository.findAllById(permissionIds);
            for (Permission permission : permissions) {
                if ((!PermCategoryEnum.GENERAL.equals(permission.getCategory()) && !account.getAccountType().name().equals(permission.getCategory().name()))
                        || PermissionEnum.SUPER_PERMISSION.name().equals(permission.getName())) {
                    throw new BadRequestException("Incorrect permission provided");
                }
            }
        }
        PowerValidator.notEmpty(permissions, ErrorMessages.PERMISSIONS_REQUIRED_FOR_ROLE);
        return permissions;
    }
}
