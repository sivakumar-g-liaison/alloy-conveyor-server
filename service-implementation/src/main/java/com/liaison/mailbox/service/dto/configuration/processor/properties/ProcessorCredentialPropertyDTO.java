package com.liaison.mailbox.service.dto.configuration.processor.properties;


/**
 * Data Transfer Object for processor credential details.
 * 
 * @author OFS
 */
public class ProcessorCredentialPropertyDTO {

    private String credentialType;
    private String credentialDisplayType;
    private String credentialURI;
    private String userId;
    private String password;
    private String idpType;
    private String idpURI;
    private boolean valueProvided;
    
    public String getCredentialType() {
        return credentialType;
    }
    public void setCredentialType(String credentialType) {
        this.credentialType = credentialType;
    }
    public String getCredentialDisplayType() {
        return credentialDisplayType;
    }
    public void setCredentialDisplayType(String credentialDisplayType) {
        this.credentialDisplayType = credentialDisplayType;
    }
    public String getCredentialURI() {
        return credentialURI;
    }
    public void setCredentialURI(String credentialURI) {
        this.credentialURI = credentialURI;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getIdpType() {
        return idpType;
    }
    public void setIdpType(String idpType) {
        this.idpType = idpType;
    }
    public String getIdpURI() {
        return idpURI;
    }
    public void setIdpURI(String idpURI) {
        this.idpURI = idpURI;
    }
    public boolean isValueProvided() {
        return valueProvided;
    }
    public void setValueProvided(boolean valueProvided) {
        this.valueProvided = valueProvided;
    }
    
    
}
