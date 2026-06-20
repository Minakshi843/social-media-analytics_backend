package com.social.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TargetRequest {

    @NotBlank(message = "Platform is required")
    private String platform;

    @NotNull(message = "Daily static target is required")
    private Integer dailyStaticTarget;

    @NotNull(message = "Daily carousel target is required")
    private Integer dailyCarouselTarget;

    @NotNull(message = "Daily reel target is required")
    private Integer dailyReelTarget;

    @NotNull(message = "Daily post target is required")
    private Integer dailyPostTarget;

    public TargetRequest() {
    }

    public TargetRequest(String platform, Integer dailyStaticTarget, Integer dailyCarouselTarget, Integer dailyReelTarget, Integer dailyPostTarget) {
        this.platform = platform;
        this.dailyStaticTarget = dailyStaticTarget;
        this.dailyCarouselTarget = dailyCarouselTarget;
        this.dailyReelTarget = dailyReelTarget;
        this.dailyPostTarget = dailyPostTarget;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Integer getDailyStaticTarget() {
        return dailyStaticTarget;
    }

    public void setDailyStaticTarget(Integer dailyStaticTarget) {
        this.dailyStaticTarget = dailyStaticTarget;
    }

    public Integer getDailyCarouselTarget() {
        return dailyCarouselTarget;
    }

    public void setDailyCarouselTarget(Integer dailyCarouselTarget) {
        this.dailyCarouselTarget = dailyCarouselTarget;
    }

    public Integer getDailyReelTarget() {
        return dailyReelTarget;
    }

    public void setDailyReelTarget(Integer dailyReelTarget) {
        this.dailyReelTarget = dailyReelTarget;
    }

    public Integer getDailyPostTarget() {
        return dailyPostTarget;
    }

    public void setDailyPostTarget(Integer dailyPostTarget) {
        this.dailyPostTarget = dailyPostTarget;
    }
}
