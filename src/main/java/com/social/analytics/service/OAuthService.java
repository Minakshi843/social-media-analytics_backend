package com.social.analytics.service;

public interface OAuthService {
    String getAuthorizationUrl(String platform, Long cityId);
    void handleCallback(String platform, String code, Long cityId);
    void disconnect(String platform, Long cityId);
    void refreshPlatformToken(Long accountId);
}
