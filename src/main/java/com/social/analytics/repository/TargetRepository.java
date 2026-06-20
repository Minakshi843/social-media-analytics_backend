package com.social.analytics.repository;

import com.social.analytics.model.Target;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TargetRepository extends JpaRepository<Target, Long> {
    List<Target> findByCityId(Long cityId);
    Optional<Target> findByCityIdAndPlatform(Long cityId, String platform);
}
