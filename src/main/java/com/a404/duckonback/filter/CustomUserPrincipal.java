package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class CustomUserPrincipal implements OAuth2User, UserDetails {
    private final User user;
    private final Map<String,Object> attributes;  // 소셜 로그인 시에만 들어옴

    public CustomUserPrincipal(User user, Map<String,Object> attributes) {
        this.user = user;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    // 일반 로그인(JDBC)용 생성자 오버로드
    public CustomUserPrincipal(User user) {
        this(user, Collections.emptyMap());
    }

    public User getUser() { return user; }
    public Long getId()     { return user.getId(); }

    // OAuth2User
    @Override public Map<String,Object> getAttributes()   { return attributes; }
    @Override public String getName()                     { return user.getUserId(); }

    // UserDetails
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    @Override public String getPassword()    { return user.getPassword(); }
    @Override public String getUsername()    { return user.getUserId(); }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()             { return true; }
}