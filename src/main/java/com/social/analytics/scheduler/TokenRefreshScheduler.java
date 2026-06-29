package com.social.analytics.scheduler;

import com.social.analytics.model.ConnectionStatus;
import com.social.analytics.model.SocialAccount;
import com.social.analytics.repository.SocialAccountRepository;
import com.social.analytics.service.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TokenRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshScheduler.class);

    private final SocialAccountRepository socialAccountRepository;
    private final OAuthService oauthService;

    @Autowired
    public TokenRefreshScheduler(SocialAccountRepository socialAccountRepository, OAuthService oauthService) {
        this.socialAccountRepository = socialAccountRepository;
        this.oauthService = oauthService;
    }

    /**
     * Periodic task to refresh connected platform tokens that are close to expiry (e.g., within 7 days).
     * Runs daily at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkAndRefreshTokens() {
        logger.info("Scanning for expiring social media OAuth tokens...");
        List<SocialAccount> accounts = socialAccountRepository.findAll();
        LocalDateTime threshold = LocalDateTime.now().plusDays(7);

        int refreshCount = 0;
        for (SocialAccount account : accounts) {
            if (account.getConnectionStatus() == ConnectionStatus.CONNECTED 
                    && account.getTokenExpiry() != null 
                    && account.getTokenExpiry().isBefore(threshold)) {
                
                logger.info("Token for social account ID {} ({}) is expiring on {}. Initiating token refresh...",
                        account.getId(), account.getPlatform(), account.getTokenExpiry());
                try {
                    oauthService.refreshPlatformToken(account.getId());
                    refreshCount++;
                } catch (Exception e) {
                    logger.error("Failed to refresh token for social account ID: " + account.getId(), e);
                }
            }
        }
        logger.info("OAuth token scan complete. Attempted to refresh {} token(s).", refreshCount);
    }
}
