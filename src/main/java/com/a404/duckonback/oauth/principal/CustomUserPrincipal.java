package com.a404.duckonback.oauth.principal;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserPrincipal implements OAuth2User, UserDetails {
    private final User user;
    private final Map<String, Object> attributes;

    public CustomUserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public Long getid() { return user.getId(); }
    public UserRole getRole() { return user.getRole(); }
    public User getUser() { return user; }

    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getName() { return user.getUserId(); } // or uuid
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUserId(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
