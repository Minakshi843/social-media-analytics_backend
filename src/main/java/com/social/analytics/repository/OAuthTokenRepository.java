package com.social.analytics.repository;

import com.social.analytics.model.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findBySocialAccountId(Long socialAccountId);
}
