package com.example.FinanceDemoApi.financeDemo.Model;

public class UserRequest {
    private String username;
    private String role;  // This role will be dynamically set by the AOP aspect

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}