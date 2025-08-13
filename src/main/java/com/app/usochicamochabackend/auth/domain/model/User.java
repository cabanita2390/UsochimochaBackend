package com.app.usochicamochabackend.auth.domain.model;

import com.app.usochicamochabackend.auth.domain.enums.RoleEnum;

public class User {
    private Long id;
    private String fullName;
    private String status;
    private String username;
    private String password;
    private String email;
    private RoleEnum role;

    public User() {
    }

    public User(Long id, String fullName, String status, String username, String password, String email, RoleEnum role) {
        this.id = id;
        this.fullName = fullName;
        this.status = status;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}