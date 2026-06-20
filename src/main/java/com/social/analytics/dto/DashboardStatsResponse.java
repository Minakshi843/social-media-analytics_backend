package com.social.analytics.dto;

import java.util.List;

public class DashboardStatsResponse {

    private long totalCities;
    private long totalAccounts;
    private long totalPosts;
    private double achievementRate;
    private long pendingTargets;

    private List<PlatformStat> platformPerformance;
    private List<CityStat> cityPerformance;
    private List<TrendStat> monthlyTrends;
    private List<TrendStat> dailyTrends;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalCities, long totalAccounts, long totalPosts, double achievementRate, long pendingTargets,
                                  List<PlatformStat> platformPerformance, List<CityStat> cityPerformance,
                                  List<TrendStat> monthlyTrends, List<TrendStat> dailyTrends) {
        this.totalCities = totalCities;
        this.totalAccounts = totalAccounts;
        this.totalPosts = totalPosts;
        this.achievementRate = achievementRate;
        this.pendingTargets = pendingTargets;
        this.platformPerformance = platformPerformance;
        this.cityPerformance = cityPerformance;
        this.monthlyTrends = monthlyTrends;
        this.dailyTrends = dailyTrends;
    }

    public long getTotalCities() {
        return totalCities;
    }

    public void setTotalCities(long totalCities) {
        this.totalCities = totalCities;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public double getAchievementRate() {
        return achievementRate;
    }

    public void setAchievementRate(double achievementRate) {
        this.achievementRate = achievementRate;
    }

    public long getPendingTargets() {
        return pendingTargets;
    }

    public void setPendingTargets(long pendingTargets) {
        this.pendingTargets = pendingTargets;
    }

    public List<PlatformStat> getPlatformPerformance() {
        return platformPerformance;
    }

    public void setPlatformPerformance(List<PlatformStat> platformPerformance) {
        this.platformPerformance = platformPerformance;
    }

    public List<CityStat> getCityPerformance() {
        return cityPerformance;
    }

    public void setCityPerformance(List<CityStat> cityPerformance) {
        this.cityPerformance = cityPerformance;
    }

    public List<TrendStat> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<TrendStat> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }

    public List<TrendStat> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<TrendStat> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    // Static nested class for Platform Statistics
    public static class PlatformStat {
        private String platform;
        private long posts;

        public PlatformStat() {
        }

        public PlatformStat(String platform, long posts) {
            this.platform = platform;
            this.posts = posts;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public long getPosts() {
            return posts;
        }

        public void setPosts(long posts) {
            this.posts = posts;
        }
    }

    // Static nested class for City Statistics
    public static class CityStat {
        private Long cityId;
        private String cityName;
        private long posts;
        private double achievementRate;
        private long pending;

        public CityStat() {
        }

        public CityStat(Long cityId, String cityName, long posts, double achievementRate, long pending) {
            this.cityId = cityId;
            this.cityName = cityName;
            this.posts = posts;
            this.achievementRate = achievementRate;
            this.pending = pending;
        }

        public Long getCityId() {
            return cityId;
        }

        public void setCityId(Long cityId) {
            this.cityId = cityId;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public long getPosts() {
            return posts;
        }

        public void setPosts(long posts) {
            this.posts = posts;
        }

        public double getAchievementRate() {
            return achievementRate;
        }

        public void setAchievementRate(double achievementRate) {
            this.achievementRate = achievementRate;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }
    }

    // Static nested class for Trend Statistics
    public static class TrendStat {
        private String label;
        private long staticCount;
        private long carouselCount;
        private long reelCount;
        private long totalCount;

        public TrendStat() {
        }

        public TrendStat(String label, long staticCount, long carouselCount, long reelCount, long totalCount) {
            this.label = label;
            this.staticCount = staticCount;
            this.carouselCount = carouselCount;
            this.reelCount = reelCount;
            this.totalCount = totalCount;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getStaticCount() {
            return staticCount;
        }

        public void setStaticCount(long staticCount) {
            this.staticCount = staticCount;
        }

        public long getCarouselCount() {
            return carouselCount;
        }

        public void setCarouselCount(long carouselCount) {
            this.carouselCount = carouselCount;
        }

        public long getReelCount() {
            return reelCount;
        }

        public void setReelCount(long reelCount) {
            this.reelCount = reelCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }
    }
}
