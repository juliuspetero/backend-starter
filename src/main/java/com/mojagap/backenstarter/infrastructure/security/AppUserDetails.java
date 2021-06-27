package com.mojagap.backenstarter.infrastructure.security;

import com.mojagap.backenstarter.model.user.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AppUserDetails extends User {

    private final AppUser appUser;

    public AppUserDetails(AppUser appUser, Collection<? extends GrantedAuthority> authorities) {
        super(appUser.getEmail(), appUser.getPassword(), authorities);
        this.appUser = appUser;
    }

    public AppUser getAppUser() {
        return appUser;
    }
}
