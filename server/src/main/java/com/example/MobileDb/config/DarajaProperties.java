package com.example.MobileDb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "daraja")
public class DarajaProperties {

    private String consumerKey;
    private String consumerSecret;
    private String tokenUrl;

    // STK Push
    private String stkPushUrl;   // <-- missing before
    private String passkey;
    private String stkResultUrl;
    private String stkTimeoutUrl;

    // B2C
    private String b2cUrl;
    private String b2cResultUrl;
    private String b2cTimeoutUrl;

    // Initiator credentials
    private String initiatorName;
    private String initiatorPassword;
    private String certificatePath;

    private String shortcode;

    // optional sandbox test parties
    private String partyA;
    private String partyB;
    private String b2cShortcode;

    // --- Getters and setters ---
    public String getConsumerKey() { return consumerKey; }
    public void setConsumerKey(String consumerKey) { this.consumerKey = consumerKey; }

    public String getConsumerSecret() { return consumerSecret; }
    public void setConsumerSecret(String consumerSecret) { this.consumerSecret = consumerSecret; }

    public String getTokenUrl() { return tokenUrl; }
    public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }

    public String getStkPushUrl() { return stkPushUrl; }
    public void setStkPushUrl(String stkPushUrl) { this.stkPushUrl = stkPushUrl; }

    public String getPasskey() { return passkey; }
    public void setPasskey(String passkey) { this.passkey = passkey; }

    public String getStkResultUrl() { return stkResultUrl; }
    public void setStkResultUrl(String stkResultUrl) { this.stkResultUrl = stkResultUrl; }

    public String getStkTimeoutUrl() { return stkTimeoutUrl; }
    public void setStkTimeoutUrl(String stkTimeoutUrl) { this.stkTimeoutUrl = stkTimeoutUrl; }

    public String getB2cUrl() { return b2cUrl; }
    public void setB2cUrl(String b2cUrl) { this.b2cUrl = b2cUrl; }

    public String getB2cResultUrl() { return b2cResultUrl; }
    public void setB2cResultUrl(String b2cResultUrl) { this.b2cResultUrl = b2cResultUrl; }

    public String getB2cTimeoutUrl() { return b2cTimeoutUrl; }
    public void setB2cTimeoutUrl(String b2cTimeoutUrl) { this.b2cTimeoutUrl = b2cTimeoutUrl; }

    public String getInitiatorName() { return initiatorName; }
    public void setInitiatorName(String initiatorName) { this.initiatorName = initiatorName; }

    public String getInitiatorPassword() { return initiatorPassword; }
    public void setInitiatorPassword(String initiatorPassword) { this.initiatorPassword = initiatorPassword; }

    public String getCertificatePath() { return certificatePath; }
    public void setCertificatePath(String certificatePath) { this.certificatePath = certificatePath; }

    public String getShortcode() { return shortcode; }
    public void setShortcode(String shortcode) { this.shortcode = shortcode; }

    public String getPartyA() { return partyA; }
    public void setPartyA(String partyA) { this.partyA = partyA; }

    public String getPartyB() { return partyB; }
    public void setPartyB(String partyB) { this.partyB = partyB; }
    
 
    public String getB2cShortcode() { return b2cShortcode; }
    public void setB2cShortcode(String b2cShortcode) { this.b2cShortcode = b2cShortcode; }

}
