package com.social.analytics.service;

import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.City;
import com.social.analytics.model.ConnectionStatus;
import com.social.analytics.model.SocialAccount;
import com.social.analytics.repository.CityRepository;
import com.social.analytics.repository.OAuthTokenRepository;
import com.social.analytics.repository.SocialAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@SuppressWarnings({"null", "rawtypes", "unchecked"})
public class OAuthServiceImpl implements OAuthService {

    private static final Logger logger = LoggerFactory.getLogger(OAuthServiceImpl.class);

    private final SocialAccountRepository socialAccountRepository;
    private final CityRepository cityRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth.instagram.client-id:instagram_dummy_id}")
    private String instagramClientId;
    @Value("${oauth.instagram.client-secret:instagram_dummy_secret}")
    private String instagramClientSecret;
    @Value("${oauth.instagram.redirect-uri:http://localhost:8080/api/oauth/instagram/callback}")
    private String instagramRedirectUri;

    @Value("${oauth.facebook.client-id:facebook_dummy_id}")
    private String facebookClientId;
    @Value("${oauth.facebook.client-secret:facebook_dummy_secret}")
    private String facebookClientSecret;
    @Value("${oauth.facebook.redirect-uri:http://localhost:8080/api/oauth/facebook/callback}")
    private String facebookRedirectUri;

    @Value("${oauth.linkedin.client-id:linkedin_dummy_id}")
    private String linkedinClientId;
    @Value("${oauth.linkedin.client-secret:linkedin_dummy_secret}")
    private String linkedinClientSecret;
    @Value("${oauth.linkedin.redirect-uri:http://localhost:8080/api/oauth/linkedin/callback}")
    private String linkedinRedirectUri;

    @Value("${oauth.x.client-id:x_dummy_id}")
    private String xClientId;
    @Value("${oauth.x.client-secret:x_dummy_secret}")
    private String xClientSecret;
    @Value("${oauth.x.redirect-uri:http://localhost:8080/api/oauth/x/callback}")
    private String xRedirectUri;

    @Autowired
    public OAuthServiceImpl(SocialAccountRepository socialAccountRepository, CityRepository cityRepository, OAuthTokenRepository oAuthTokenRepository) {
        this.socialAccountRepository = socialAccountRepository;
        this.cityRepository = cityRepository;
        this.oAuthTokenRepository = oAuthTokenRepository;
    }

    @Override
    public String getAuthorizationUrl(String platform, Long cityId) {
        String plat = platform.toUpperCase();
        switch (plat) {
            case "INSTAGRAM":
                return String.format("https://api.instagram.com/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user_profile,user_media&response_type=code&state=%d",
                        instagramClientId, instagramRedirectUri, cityId);
            case "FACEBOOK":
                return String.format("https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&scope=pages_manage_posts,pages_read_engagement&response_type=code&state=%d",
                        facebookClientId, facebookRedirectUri, cityId);
            case "LINKEDIN":
                return String.format("https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%d&scope=r_liteprofile%%20r_emailaddress",
                        linkedinClientId, linkedinRedirectUri, cityId);
            case "X":
                return String.format("https://twitter.com/i/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=tweet.read%%20users.read%%20offline.access&state=%d&code_challenge=challenge&code_challenge_method=plain",
                        xClientId, xRedirectUri, cityId);
            default:
                throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }

    @Override
    public void handleCallback(String platform, String code, Long cityId) {
        String plat = platform.toUpperCase();
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));

        String clientId = getClientId(plat);
        String clientSecret = getClientSecret(plat);
        String redirectUri = getRedirectUri(plat);

        SocialAccount account = socialAccountRepository.findFirstByCityIdAndPlatform(cityId, plat)
                .orElse(new SocialAccount(city, plat, city.getName() + " " + plat, "@" + city.getName().toLowerCase() + "_" + plat.toLowerCase(), "https://" + plat.toLowerCase() + ".com/" + city.getName().toLowerCase(), ConnectionStatus.NOT_CONNECTED));

        // Detect if credentials are dummy, trigger simulation
        if (clientId == null || clientId.contains("dummy") || code.equals("simulated_code")) {
            logger.info("Simulating OAuth token exchange for platform: {} and cityId: {}", plat, cityId);
            account.setAccessToken("simulated_access_token_" + UUID.randomUUID());
            account.setRefreshToken("simulated_refresh_token_" + UUID.randomUUID());
            account.setTokenExpiry(LocalDateTime.now().plusDays(60));
            account.setConnectionStatus(ConnectionStatus.CONNECTED);
            socialAccountRepository.save(account);
            return;
        }

        try {
            String tokenUrl = getTokenUrl(plat);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                String accessToken = (String) respBody.get("access_token");
                String refreshToken = (String) respBody.get("refresh_token");
                Integer expiresIn = (Integer) respBody.get("expires_in");

                account.setAccessToken(accessToken);
                if (refreshToken != null) {
                    account.setRefreshToken(refreshToken);
                }
                if (expiresIn != null) {
                    account.setTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
                } else {
                    account.setTokenExpiry(LocalDateTime.now().plusDays(60));
                }
                account.setConnectionStatus(ConnectionStatus.CONNECTED);
                socialAccountRepository.save(account);
                logger.info("Successfully connected OAuth account for {} on {}", city.getName(), plat);
            } else {
                throw new RuntimeException("OAuth token response failed");
            }
        } catch (Exception e) {
            logger.error("OAuth token exchange failed for platform: " + plat, e);
            account.setConnectionStatus(ConnectionStatus.ERROR);
            socialAccountRepository.save(account);
            throw e;
        }
    }

    @Override
    public void disconnect(String platform, Long cityId) {
        String plat = platform.toUpperCase();
        SocialAccount account = socialAccountRepository.findFirstByCityIdAndPlatform(cityId, plat)
                .orElseThrow(() -> new ResourceNotFoundException("No social account connected for platform " + plat + " in city " + cityId));

        account.setAccessToken(null);
        account.setRefreshToken(null);
        account.setTokenExpiry(null);
        account.setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
        socialAccountRepository.save(account);

        // Delete from oauth_tokens repository as well if it exists
        oAuthTokenRepository.findBySocialAccountId(account.getId())
                .ifPresent(oAuthTokenRepository::delete);

        logger.info("Disconnected social account ID {} for platform {}", account.getId(), plat);
    }

    @Override
    public void refreshPlatformToken(Long accountId) {
        SocialAccount account = socialAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found with id: " + accountId));

        if (account.getRefreshToken() == null) {
            logger.warn("Cannot refresh token for account {}, no refresh token present", accountId);
            return;
        }

        String plat = account.getPlatform().toUpperCase();
        String clientId = getClientId(plat);
        String clientSecret = getClientSecret(plat);

        if (clientId.contains("dummy") || account.getRefreshToken().startsWith("simulated_refresh_token")) {
            logger.info("Simulating refresh token for account {}", accountId);
            account.setAccessToken("simulated_access_token_" + UUID.randomUUID());
            account.setTokenExpiry(LocalDateTime.now().plusDays(60));
            account.setConnectionStatus(ConnectionStatus.CONNECTED);
            socialAccountRepository.save(account);
            return;
        }

        try {
            String tokenUrl = getTokenUrl(plat);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", account.getRefreshToken());
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                String accessToken = (String) respBody.get("access_token");
                String newRefreshToken = (String) respBody.get("refresh_token");
                Integer expiresIn = (Integer) respBody.get("expires_in");

                account.setAccessToken(accessToken);
                if (newRefreshToken != null) {
                    account.setRefreshToken(newRefreshToken);
                }
                if (expiresIn != null) {
                    account.setTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
                }
                account.setConnectionStatus(ConnectionStatus.CONNECTED);
                socialAccountRepository.save(account);
                logger.info("Successfully refreshed token for account ID {}", accountId);
            }
        } catch (Exception e) {
            logger.error("Token refresh failed for account ID " + accountId, e);
            account.setConnectionStatus(ConnectionStatus.TOKEN_EXPIRED);
            socialAccountRepository.save(account);
        }
    }

    private String getClientId(String platform) {
        switch (platform) {
            case "INSTAGRAM": return instagramClientId;
            case "FACEBOOK": return facebookClientId;
            case "LINKEDIN": return linkedinClientId;
            case "X": return xClientId;
            default: return "";
        }
    }

    private String getClientSecret(String platform) {
        switch (platform) {
            case "INSTAGRAM": return instagramClientSecret;
            case "FACEBOOK": return facebookClientSecret;
            case "LINKEDIN": return linkedinClientSecret;
            case "X": return xClientSecret;
            default: return "";
        }
    }

    private String getRedirectUri(String platform) {
        switch (platform) {
            case "INSTAGRAM": return instagramRedirectUri;
            case "FACEBOOK": return facebookRedirectUri;
            case "LINKEDIN": return linkedinRedirectUri;
            case "X": return xRedirectUri;
            default: return "";
        }
    }

    private String getTokenUrl(String platform) {
        switch (platform) {
            case "INSTAGRAM": return "https://api.instagram.com/oauth/access_token";
            case "FACEBOOK": return "https://graph.facebook.com/v18.0/oauth/access_token";
            case "LINKEDIN": return "https://www.linkedin.com/oauth/v2/accessToken";
            case "X": return "https://api.twitter.com/2/oauth2/token";
            default: return "";
        }
    }
}
