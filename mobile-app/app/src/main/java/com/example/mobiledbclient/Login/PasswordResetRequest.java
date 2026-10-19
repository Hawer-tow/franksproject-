package com.example.mobiledbclient.Login;

public class PasswordResetRequest {
    private String newPassword;

    public PasswordResetRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

