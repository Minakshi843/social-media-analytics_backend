package com.social.analytics.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String email;

    public JwtResponse() {
    }

    public JwtResponse(String token, String username, String role, String email) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
