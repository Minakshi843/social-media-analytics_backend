package com.social.analytics.repository;

import com.social.analytics.model.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    List<SocialAccount> findByCityId(Long cityId);
    Optional<SocialAccount> findByCityIdAndPlatform(Long cityId, String platform);
}
