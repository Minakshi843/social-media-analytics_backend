package com.social.analytics.repository;

import com.social.analytics.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCityId(Long cityId);
    List<Post> findByCityIdAndPlatform(Long cityId, String platform);
    List<Post> findByPostDateBetween(LocalDate startDate, LocalDate endDate);
    List<Post> findByCityIdAndPostDateBetween(Long cityId, LocalDate startDate, LocalDate endDate);
    Optional<Post> findByPlatformAndPostId(String platform, String postId);
}
