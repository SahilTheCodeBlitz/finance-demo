package com.example.FinanceDemoApi.financeDemo.Utility;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;


    // Constructor
    public CustomAuthenticationToken(Object principal, Object credentials) {
        super(Collections.emptyList()); // No authorities initially
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false); // Set as unauthenticated initially
    }

    // Constructor for authenticated token (useful after successful authentication)
    public CustomAuthenticationToken(Object principal, Object credentials, boolean isAuthenticated) {
        super(Collections.emptyList()); // No authorities, unless you want to add them later
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(isAuthenticated); // Set as authenticated when validation is complete
    }

    // For role-based checks (if you decide to implement roles in the future)
    public CustomAuthenticationToken(Object principal, Object credentials, boolean isAuthenticated, String role) {
        super(Collections.singletonList(new SimpleGrantedAuthority(role))); // Set authority if role is provided
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(isAuthenticated); // Set as authenticated
    }

    @Override
    public Object getCredentials() {
        return credentials; // JWT token
    }

    @Override
    public Object getPrincipal() {
        return principal; // UserPrincipal object
    }
}
