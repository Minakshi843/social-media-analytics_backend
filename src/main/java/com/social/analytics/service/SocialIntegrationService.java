package com.social.analytics.service;

import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.*;
import com.social.analytics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@SuppressWarnings("null")
public class SocialIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(SocialIntegrationService.class);

    private final SocialAccountRepository socialAccountRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final CityRepository cityRepository;
    private final PostRepository postRepository;
    private final AnalyticsRepository analyticsRepository;

    @Autowired
    public SocialIntegrationService(SocialAccountRepository socialAccountRepository,
                                     OAuthTokenRepository oAuthTokenRepository,
                                     CityRepository cityRepository,
                                     PostRepository postRepository,
                                     AnalyticsRepository analyticsRepository) {
        this.socialAccountRepository = socialAccountRepository;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.cityRepository = cityRepository;
        this.postRepository = postRepository;
        this.analyticsRepository = analyticsRepository;
    }

    public List<SocialAccount> getAccountsByCity(Long cityId) {
        return socialAccountRepository.findByCityId(cityId);
    }

    public List<SocialAccount> getAllAccounts() {
        return socialAccountRepository.findAll();
    }

    @Transactional
    public SocialAccount connectAccount(Long cityId, String platform, String accountName, String accountUrl) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));

        String platformKey = platform.toUpperCase();
        SocialAccount account = socialAccountRepository.findByCityIdAndPlatform(cityId, platformKey)
                .orElse(new SocialAccount(city, accountName, accountUrl, platformKey));

        account.setAccountName(accountName);
        account.setAccountUrl(accountUrl);
        account = socialAccountRepository.save(account);

        // Store a simulated OAuth Token
        OAuthToken token = oAuthTokenRepository.findBySocialAccountId(account.getId())
                .orElse(new OAuthToken(account, "", "", LocalDateTime.now()));

        token.setAccessToken("simulated_access_token_" + UUID.randomUUID());
        token.setRefreshToken("simulated_refresh_token_" + UUID.randomUUID());
        token.setTokenExpiry(LocalDateTime.now().plusDays(60)); // 60 days expiration
        oAuthTokenRepository.save(token);

        logger.info("Connected social account {} on platform {} for city {}", accountName, platformKey, city.getName());
        return account;
    }

    @Transactional
    public void disconnectAccount(Long accountId) {
        SocialAccount account = socialAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found with id: " + accountId));
        socialAccountRepository.delete(account);
    }

    /**
     * Synchronize post data for all connected accounts.
     * Fetches mock posts for connected platforms and aggregates analytics.
     */
    @Transactional
    public void syncAllAccounts() {
        logger.info("Starting global synchronization of social media posts...");
        List<SocialAccount> accounts = socialAccountRepository.findAll();
        for (SocialAccount account : accounts) {
            syncAccount(account);
        }
        logger.info("Global synchronization complete.");
    }

    @Transactional
    public void syncCityAccounts(Long cityId) {
        logger.info("Starting synchronization of social media posts for City ID: {}", cityId);
        List<SocialAccount> accounts = socialAccountRepository.findByCityId(cityId);
        for (SocialAccount account : accounts) {
            syncAccount(account);
        }
        logger.info("Synchronization for City ID: {} complete.", cityId);
    }

    private void syncAccount(SocialAccount account) {
        // Fetch raw posts from platform (Simulated)
        List<RawPost> rawPosts = fetchSimulatedRawPosts(account);

        for (RawPost raw : rawPosts) {
            // Apply Post Classification Rules
            String classifiedType = classifyPost(account.getPlatform(), raw.getMediaType(), raw.getAttachmentCount());

            // Save or Update Post in DB
            Post post = postRepository.findByPlatformAndPostId(account.getPlatform(), raw.getId())
                    .orElse(new Post());

            post.setPostId(raw.getId());
            post.setPlatform(account.getPlatform());
            post.setCity(account.getCity());
            post.setPostUrl(raw.getUrl());
            post.setCaption(raw.getCaption());
            post.setPostDate(raw.getDate());
            post.setPostTime(raw.getTime());
            post.setPostType(classifiedType);
            post.setLikes(raw.getLikes());
            post.setComments(raw.getComments());
            post.setReach(raw.getReach());
            post.setImpressions(raw.getImpressions());

            postRepository.save(post);
        }

        // Reaggregate daily counts for this account's city and platform
        aggregateAnalyticsForCityAndPlatform(account.getCity(), account.getPlatform());
    }

    /**
     * Post Classification Rules logic:
     * Instagram: IMAGE -> Static, VIDEO -> Reel, CAROUSEL_ALBUM -> Carousel
     * Facebook: Single Image (attachment=1) -> Static, Multiple Images (attachment>1) -> Carousel, Video -> Reel
     * X & LinkedIn: IMAGE -> Static, VIDEO -> Reel, CAROUSEL -> Carousel
     */
    public String classifyPost(String platform, String mediaType, int attachmentCount) {
        String plat = platform.toUpperCase();
        if ("INSTAGRAM".equals(plat)) {
            if ("VIDEO".equalsIgnoreCase(mediaType)) {
                return "REEL";
            } else if ("CAROUSEL_ALBUM".equalsIgnoreCase(mediaType)) {
                return "CAROUSEL";
            } else {
                return "STATIC";
            }
        } else if ("FACEBOOK".equals(plat)) {
            if ("VIDEO".equalsIgnoreCase(mediaType)) {
                return "REEL";
            } else if ("CAROUSEL".equalsIgnoreCase(mediaType) || attachmentCount > 1) {
                return "CAROUSEL";
            } else {
                return "STATIC";
            }
        } else {
            // Default for X and LinkedIn
            if ("VIDEO".equalsIgnoreCase(mediaType)) {
                return "REEL";
            } else if ("CAROUSEL".equalsIgnoreCase(mediaType) || attachmentCount > 1) {
                return "CAROUSEL";
            } else {
                return "STATIC";
            }
        }
    }

    @Transactional
    public void aggregateAnalyticsForCityAndPlatform(City city, String platform) {
        // Clear and reaggregate analytics for the last 30 days to keep pre-aggregated data fresh
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Post> posts = postRepository.findByCityIdAndPostDateBetween(city.getId(), date, date);

            int staticCount = 0;
            int carouselCount = 0;
            int reelCount = 0;
            int total = 0;

            for (Post p : posts) {
                if (p.getPlatform().equalsIgnoreCase(platform)) {
                    total++;
                    if ("STATIC".equalsIgnoreCase(p.getPostType())) {
                        staticCount++;
                    } else if ("CAROUSEL".equalsIgnoreCase(p.getPostType())) {
                        carouselCount++;
                    } else if ("REEL".equalsIgnoreCase(p.getPostType())) {
                        reelCount++;
                    }
                }
            }

            if (total > 0) {
                Analytics analytics = analyticsRepository.findByCityIdAndDateAndPlatform(city.getId(), date, platform)
                        .orElse(new Analytics(city, date, platform, 0, 0, 0, 0));

                analytics.setStaticCount(staticCount);
                analytics.setCarouselCount(carouselCount);
                analytics.setReelCount(reelCount);
                analytics.setPostCount(total);

                analyticsRepository.save(analytics);
            }
        }
    }

    /**
     * Generates simulated posts for a connected platform spanning the past 30 days.
     */
    private List<RawPost> fetchSimulatedRawPosts(SocialAccount account) {
        List<RawPost> rawPosts = new ArrayList<>();
        Random rand = new Random(account.getId() + 99); // stable seed for consistency
        LocalDate today = LocalDate.now();

        // Platform specific rules
        String[] types;
        if ("INSTAGRAM".equalsIgnoreCase(account.getPlatform())) {
            types = new String[]{"IMAGE", "VIDEO", "CAROUSEL_ALBUM"};
        } else if ("FACEBOOK".equalsIgnoreCase(account.getPlatform())) {
            types = new String[]{"IMAGE", "VIDEO", "CAROUSEL"};
        } else {
            types = new String[]{"IMAGE", "VIDEO", "CAROUSEL"};
        }

        // Generate 1-3 posts per day for the last 30 days
        for (int i = 0; i <= 30; i++) {
            LocalDate date = today.minusDays(i);
            int postCount = rand.nextInt(3); // 0, 1, or 2 posts
            for (int p = 0; p < postCount; p++) {
                String mediaType = types[rand.nextInt(types.length)];
                int attachments = "CAROUSEL".equals(mediaType) ? rand.nextInt(4) + 2 : 1;
                String postId = "ext_" + account.getPlatform().toLowerCase() + "_" + date.toString().replace("-", "") + "_" + p;
                
                int likes = rand.nextInt(400) + 50;
                int comments = rand.nextInt(80) + 5;
                int reach = likes * (rand.nextInt(10) + 3);
                int impressions = (int)(reach * (1.1 + rand.nextDouble() * 0.5));

                String caption = "Simulated update from " + account.getCity().getName() + " on " + account.getPlatform() + ". Let's make progress! #" + account.getCity().getName().toLowerCase();
                String url = "https://" + account.getPlatform().toLowerCase() + ".com/posts/" + postId;
                LocalTime time = LocalTime.of(rand.nextInt(12) + 8, rand.nextInt(60));

                rawPosts.add(new RawPost(postId, mediaType, attachments, url, caption, date, time, likes, comments, reach, impressions));
            }
        }
        return rawPosts;
    }

    // Helper class representing external platform data before classification
    private static class RawPost {
        private String id;
        private String mediaType;
        private int attachmentCount;
        private String url;
        private String caption;
        private LocalDate date;
        private LocalTime time;
        private int likes;
        private int comments;
        private int reach;
        private int impressions;

        public RawPost(String id, String mediaType, int attachmentCount, String url, String caption, LocalDate date, LocalTime time, int likes, int comments, int reach, int impressions) {
            this.id = id;
            this.mediaType = mediaType;
            this.attachmentCount = attachmentCount;
            this.url = url;
            this.caption = caption;
            this.date = date;
            this.time = time;
            this.likes = likes;
            this.comments = comments;
            this.reach = reach;
            this.impressions = impressions;
        }

        public String getId() { return id; }
        public String getMediaType() { return mediaType; }
        public int getAttachmentCount() { return attachmentCount; }
        public String getUrl() { return url; }
        public String getCaption() { return caption; }
        public LocalDate getDate() { return date; }
        public LocalTime getTime() { return time; }
        public int getLikes() { return likes; }
        public int getComments() { return comments; }
        public int getReach() { return reach; }
        public int getImpressions() { return impressions; }
    }
}
