package com.org.group.role;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    CLIENT,
    ANALYZER,
    ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}