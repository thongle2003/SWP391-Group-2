package com.evtrading.swp391.dto;

public class RegisterRequestDTO {
    private String username;
    private String email;
    private String password;
    private Integer roleID;
    private String status;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Integer getRoleID() { return roleID; }
    public void setRoleID(Integer roleID) { this.roleID = roleID; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
