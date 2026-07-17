package com.coldchain.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 登录用户
 */
@Getter
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String realName;
    private final String roleCode;
    private final List<String> permissions;
    private final boolean enabled;

    public LoginUser(Long userId, String username, String password, String realName,
                     String roleCode, List<String> permissions, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.roleCode = roleCode;
        this.permissions = permissions;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
