package com.social.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SocialAccountCreateRequest {

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Account handle is required")
    private String accountHandle;

    @NotBlank(message = "Account URL is required")
    private String accountUrl;

    @NotBlank(message = "Platform is required")
    @Pattern(regexp = "INSTAGRAM|FACEBOOK|LINKEDIN|X", message = "Platform must be INSTAGRAM, FACEBOOK, LINKEDIN, or X")
    private String platform;

    @NotNull(message = "City ID is required")
    private Long cityId;

    public SocialAccountCreateRequest() {
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }
}
