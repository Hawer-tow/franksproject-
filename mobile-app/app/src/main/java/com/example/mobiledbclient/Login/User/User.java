package com.example.mobiledbclient.Login.User;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "users")
public class User implements Serializable {
    @PrimaryKey
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("passwordHash")
    private String passwordHash;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("lastLogin")
    private String lastLogin;

    @SerializedName("biometricEnabled")
    private boolean biometricEnabled;

    public User() {} // Room requires a no-arg constructor

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBiometricEnabled() { return biometricEnabled; }
    public void setBiometricEnabled(boolean biometricEnabled) { this.biometricEnabled = biometricEnabled; }
}
