package com.social.analytics.controller;

import com.social.analytics.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);

    private final OAuthService oauthService;

    @Value("${oauth.instagram.client-id:instagram_dummy_id}")
    private String instagramClientId;
    @Value("${oauth.facebook.client-id:facebook_dummy_id}")
    private String facebookClientId;
    @Value("${oauth.linkedin.client-id:linkedin_dummy_id}")
    private String linkedinClientId;
    @Value("${oauth.x.client-id:x_dummy_id}")
    private String xClientId;

    @Value("${oauth.frontend-redirect-uri:http://localhost:3000/social-accounts}")
    private String frontendRedirectUri;

    @Autowired
    public OAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/{platform}/authorize")
    public void authorize(@PathVariable String platform,
                          @RequestParam Long cityId,
                          HttpServletResponse response) throws IOException {
        String plat = platform.toUpperCase();
        String clientId = getClientId(plat);

        // If credentials are dummy, skip third-party redirection and directly simulate callback
        if (clientId == null || clientId.contains("dummy")) {
            logger.info("Dummy Client ID detected for {}. Redirecting directly to simulation callback...", plat);
            response.sendRedirect(String.format("/api/oauth/%s/callback?code=simulated_code&state=%d", platform.toLowerCase(), cityId));
            return;
        }

        String authUrl = oauthService.getAuthorizationUrl(plat, cityId);
        logger.info("Redirecting user to authorize URL for platform {}: {}", plat, authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/{platform}/callback")
    public void callback(@PathVariable String platform,
                         @RequestParam String code,
                         @RequestParam("state") Long cityId,
                         HttpServletResponse response) throws IOException {
        String plat = platform.toUpperCase();
        try {
            oauthService.handleCallback(plat, code, cityId);
            response.sendRedirect(frontendRedirectUri + "?status=success&platform=" + plat);
        } catch (Exception e) {
            logger.error("OAuth callback failed for " + plat, e);
            response.sendRedirect(frontendRedirectUri + "?status=error&platform=" + plat);
        }
    }

    @PostMapping("/{platform}/disconnect")
    public ResponseEntity<Void> disconnect(@PathVariable String platform,
                                           @RequestParam Long cityId) {
        String plat = platform.toUpperCase();
        oauthService.disconnect(plat, cityId);
        return ResponseEntity.ok().build();
    }

    private String getClientId(String platform) {
        switch (platform) {
            case "INSTAGRAM": return instagramClientId;
            case "FACEBOOK": return facebookClientId;
            case "LINKEDIN": return linkedinClientId;
            case "X": return xClientId;
            default: return null;
        }
    }

}
