package com.example.mobiledbclient.Login;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("biometricLogin")
    private boolean biometricLogin;

    @SerializedName("biometricToken")
    private String biometricToken;

    @SerializedName("deviceId")   // ✅ match server camelCase
    private String deviceId;

    @SerializedName("biometricKeyAlias")
    private String biometricKeyAlias;

    public LoginRequest() {
    }

    // Password login constructor
    public LoginRequest(String username, String password, String deviceId) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
        this.biometricLogin = false;
    }

    // Biometric login constructor
    public LoginRequest(String username, boolean biometricLogin, String biometricToken,
                        String deviceId, String biometricKeyAlias) {
        this.username = username;
        this.biometricLogin = biometricLogin;
        this.biometricToken = biometricToken;
        this.deviceId = deviceId;
        this.biometricKeyAlias = biometricKeyAlias;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isBiometricLogin() { return biometricLogin; }
    public void setBiometricLogin(boolean biometricLogin) { this.biometricLogin = biometricLogin; }

    public String getBiometricToken() { return biometricToken; }
    public void setBiometricToken(String biometricToken) { this.biometricToken = biometricToken; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getBiometricKeyAlias() { return biometricKeyAlias; }
    public void setBiometricKeyAlias(String biometricKeyAlias) { this.biometricKeyAlias = biometricKeyAlias; }
}
