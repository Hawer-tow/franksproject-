package com.example.MobileDb.dto.kisenyi;

public class LoginRequest {
    private String username;
    private String password;
    private boolean biometricLogin;   // ✅ flag to indicate biometric login attempt
    private String biometricToken;    // ✅ optional signed token from client
    private String deviceId;    
    private String biometricKeyAlias;// ✅ device binding for normal users

    // Default constructor
    public LoginRequest() {}

    // Constructor with fields
    public LoginRequest(String username, String password, boolean biometricLogin, String biometricToken, String deviceId,  String biometricKeyAlias) {
        this.username = username;
        this.password = password;
        this.biometricLogin = biometricLogin;
        this.biometricToken = biometricToken;
        this.deviceId = deviceId;
        this.biometricKeyAlias = biometricKeyAlias;
    }

    // Getters and setters
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

    public boolean isBiometricLogin() {
        return biometricLogin;
    }

    public void setBiometricLogin(boolean biometricLogin) {
        this.biometricLogin = biometricLogin;
    }

    public String getBiometricToken() {
        return biometricToken;
    }

    public void setBiometricToken(String biometricToken) {
        this.biometricToken = biometricToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBiometricKeyAlias() {
    	return biometricKeyAlias;
    	}   // ✅ new getter
    public void setBiometricKeyAlias(String biometricKeyAlias) { 
    	this.biometricKeyAlias = biometricKeyAlias; 
    	} // ✅ new setter
}
