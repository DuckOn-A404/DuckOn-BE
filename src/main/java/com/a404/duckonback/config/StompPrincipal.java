package com.a404.duckonback.config;

public class StompPrincipal implements java.security.Principal {
    private final String name;
    public StompPrincipal(String name) { this.name = name; }
    @Override public String getName() { return name; }
}
