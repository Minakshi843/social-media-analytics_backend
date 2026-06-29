package com.social.analytics.dto;

import jakarta.validation.constraints.NotBlank;

public class SocialAccountUpdateRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Account handle is required")
    private String accountHandle;

    @NotBlank(message = "Account URL is required")
    private String accountUrl;

    public SocialAccountUpdateRequest() {
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountHandle() {
        return accountHandle;
    }

    public void setAccountHandle(String accountHandle) {
        this.accountHandle = accountHandle;
    }

    public String getAccountUrl() {
        return accountUrl;
    }

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }
}
